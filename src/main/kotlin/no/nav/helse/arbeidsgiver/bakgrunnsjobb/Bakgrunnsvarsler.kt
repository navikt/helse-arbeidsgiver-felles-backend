package no.nav.helse.arbeidsgiver.bakgrunnsjobb

interface Bakgrunnsvarsler {

    fun rapporterPermanentFeiletJobb()
}

class TomVarsler() : Bakgrunnsvarsler {
    override fun rapporterPermanentFeiletJobb() {
    }
}
