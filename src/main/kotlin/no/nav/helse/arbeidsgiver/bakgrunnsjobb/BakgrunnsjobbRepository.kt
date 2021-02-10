package no.nav.helse.arbeidsgiver.bakgrunnsjobb

import no.nav.helse.arbeidsgiver.processing.AutoCleanJobbProcessor
import java.sql.Connection
import java.sql.Date
import java.sql.ResultSet
import java.sql.Timestamp
import java.time.Instant.now
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.sql.DataSource

interface BakgrunnsjobbRepository {
    fun getById(id: UUID) : Bakgrunnsjobb
    fun save(bakgrunnsjobb: Bakgrunnsjobb)
    fun save(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection)
    fun findAutoCleanJobs(): List<Bakgrunnsjobb>
    fun findByKjoeretidBeforeAndStatusIn(timeout: LocalDateTime, tilstander: Set<BakgrunnsjobbStatus>): List<Bakgrunnsjobb>
    fun delete(uuid: UUID)
    fun deleteAll()
    fun deleteOldOkJobs(months : Long)
    fun update(bakgrunnsjobb: Bakgrunnsjobb)
    fun update(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection)
}

class MockBakgrunnsjobbRepository : BakgrunnsjobbRepository {

    private val jobs = mutableListOf<Bakgrunnsjobb>()

    override fun findAutoCleanJobs(): List<Bakgrunnsjobb> {
        return jobs.filter { it.type.equals(AutoCleanJobbProcessor.JOB_TYPE) }
    }

    override fun getById(id: UUID): Bakgrunnsjobb {
        return jobs.filter{it.uuid.equals(id)}.get(0)
    }

    override fun save(bakgrunnsjobb: Bakgrunnsjobb) {
        jobs.add(bakgrunnsjobb)
    }

    override fun save(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection) {
        jobs.add(bakgrunnsjobb)
    }

    override fun findByKjoeretidBeforeAndStatusIn(timeout: LocalDateTime, tilstander: Set<BakgrunnsjobbStatus>): List<Bakgrunnsjobb> {
        return jobs.filter { tilstander.contains(it.status) }
                .filter { it.kjoeretid.isBefore(timeout) }
    }

    override fun delete(uuid: UUID) {
        jobs.removeIf { it.uuid == uuid }
    }

    override fun deleteAll() {
        jobs.removeAll { true }
    }

    override fun update(bakgrunnsjobb: Bakgrunnsjobb) {
        delete(bakgrunnsjobb.uuid)
        save(bakgrunnsjobb)
    }

    override fun update(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection) {
        update(bakgrunnsjobb)
    }

    override fun deleteOldOkJobs(months: Long) {
        val someMonthsAgo = LocalDateTime.now().minusMonths(months)
        jobs.removeIf{ it.behandlet?.isBefore(someMonthsAgo)!! && it.status.equals(BakgrunnsjobbStatus.OK) }
    }

}


class PostgresBakgrunnsjobbRepository(val dataSource: DataSource) : BakgrunnsjobbRepository {
    private val tableName = "bakgrunnsjobb"

    private val insertStatement = """INSERT INTO $tableName
    (jobb_id, type, behandlet, opprettet, status, kjoeretid, forsoek, maks_forsoek, data) VALUES
    (?::uuid,?,?,?,?,?,?,?,?::json)"""
            .trimIndent()

    private val updateStatement = """UPDATE $tableName
        SET behandlet = ?
         , status = ?
         , kjoeretid = ?
         , forsoek = ?
         , data = ?::json
        where jobb_id = ?::uuid"""
        .trimIndent()

    private val selectStatement = """
        select * from $tableName where kjoeretid < ? and status = ANY(?)
    """.trimIndent()

    private val selectByIdStatement = """
        select * from $tableName where jobb_id = ?
    """.trimIndent()


    private val selectAutoClean = """SELECT * from $tableName WHERE status IN ('OK') AND type = 'bakgrunnsjobb-autoclean'""".trimIndent()

    private val deleteStatement = "DELETE FROM $tableName where jobb_id = ?::uuid"

    private val deleteOldJobsStatement = """DELETE FROM $tableName WHERE status = 'OK' AND behandlet < ?""".trimIndent()

    private val deleteAllStatement = "DELETE FROM $tableName"

    override fun getById(id: UUID): Bakgrunnsjobb {
        dataSource.connection.use {
            val res = it.prepareStatement(selectByIdStatement).executeQuery()
            return resultsetTilResultatliste(res).get(0)
            }
}

    override fun save(bakgrunnsjobb: Bakgrunnsjobb) {
        dataSource.connection.use {
            save(bakgrunnsjobb, it)
        }
    }

    override fun save(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection) {
        connection.prepareStatement(insertStatement).apply {
            setString(1, bakgrunnsjobb.uuid.toString())
            setString(2, bakgrunnsjobb.type)
            setTimestamp(3, bakgrunnsjobb.behandlet?.let(Timestamp::valueOf))
            setTimestamp(4, Timestamp.valueOf(bakgrunnsjobb.opprettet))
            setString(5, bakgrunnsjobb.status.toString())
            setTimestamp(6, Timestamp.valueOf(bakgrunnsjobb.kjoeretid))
            setInt(7, bakgrunnsjobb.forsoek)
            setInt(8, bakgrunnsjobb.maksAntallForsoek)
            setString(9, bakgrunnsjobb.data)
        }.executeUpdate()
    }

    override fun update(bakgrunnsjobb: Bakgrunnsjobb) {
        dataSource.connection.use {
            update(bakgrunnsjobb, it)
        }
    }

    override fun update(bakgrunnsjobb: Bakgrunnsjobb, connection: Connection) {
        connection.prepareStatement(updateStatement).apply {
            setTimestamp(1, bakgrunnsjobb.behandlet?.let(Timestamp::valueOf))
            setString(2, bakgrunnsjobb.status.toString())
            setTimestamp(3, Timestamp.valueOf(bakgrunnsjobb.kjoeretid))
            setInt(4, bakgrunnsjobb.forsoek)
            setString(5, bakgrunnsjobb.data)
            setString(6, bakgrunnsjobb.uuid.toString())
        }.executeUpdate()
    }

    override fun findAutoCleanJobs(): List<Bakgrunnsjobb> {
        dataSource.connection.use {
            val res = it.prepareStatement(selectAutoClean).executeQuery()

            return resultsetTilResultatliste(res)
        }
    }

    override fun findByKjoeretidBeforeAndStatusIn(timeout: LocalDateTime, tilstander: Set<BakgrunnsjobbStatus>): List<Bakgrunnsjobb> {
        dataSource.connection.use {
            val res = it.prepareStatement(selectStatement).apply {
                setTimestamp(1, Timestamp.valueOf(timeout))
                setArray(2, it.createArrayOf("VARCHAR", tilstander.map { it.toString() }.toTypedArray()))
            }.executeQuery()

            return resultsetTilResultatliste(res)
        }
    }

    private fun resultsetTilResultatliste(res : ResultSet): MutableList<Bakgrunnsjobb> {
        val resultatListe = mutableListOf<Bakgrunnsjobb>()

        while (res.next()) {
            resultatListe.add(Bakgrunnsjobb(
                    UUID.fromString(res.getString("jobb_id")),
                    res.getString("type"),
                    res.getTimestamp("behandlet")?.toLocalDateTime(),
                    res.getTimestamp("opprettet").toLocalDateTime(),
                    BakgrunnsjobbStatus.valueOf(res.getString("status")),
                    res.getTimestamp("kjoeretid").toLocalDateTime(),
                    res.getInt("forsoek"),
                    res.getInt("maks_forsoek"),
                    res.getString("data")
            ))
        }
        return resultatListe
    }

    override fun delete(uuid: UUID) {
        dataSource.connection.use {
            it.prepareStatement(deleteStatement).apply {
                setString(1, uuid.toString())
            }.executeUpdate()
        }
    }

    override fun deleteAll() {
        dataSource.connection.use {
            it.prepareStatement(deleteAllStatement).executeUpdate()
        }
    }

    override fun deleteOldOkJobs(months: Long) {
        dataSource.connection.use {
            it.prepareStatement(deleteOldJobsStatement).apply {
                setDate(1, Date.valueOf(LocalDate.now().minusMonths(months)))
            }.executeUpdate()
        }
    }
}
