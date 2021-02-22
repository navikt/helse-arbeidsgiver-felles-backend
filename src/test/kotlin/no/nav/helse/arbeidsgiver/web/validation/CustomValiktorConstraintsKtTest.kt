package no.nav.helse.arbeidsgiver.web.validation

import no.nav.helse.arbeidsgiver.TestData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.valiktor.ConstraintViolationException

import org.valiktor.validate

internal class CustomValiktorConstraintsKtTest {
    internal data class ValidationData(val value: String)

    @Test
    fun isValidIdentitetsnummer_OK() {
        validate(ValidationData(TestData.validIdentitetsnummer)) {
            validate(ValidationData::value).isValidIdentitetsnummer()
        }
    }

    @Test
    fun isValidIdentitetsnummer_Invalid() {
        assertThrows<ConstraintViolationException> {
            validate(ValidationData(TestData.notValidIdentitetsnummer)) {
                validate(ValidationData::value).isValidIdentitetsnummer()
            }
        }
    }

    @Test
    fun isValidIdentitetsnummer_Invalid_2checksum() {
        assertThrows<ConstraintViolationException> {
            validate(ValidationData(TestData.notValidIdentitetsnummerInvalidCheckSum2)) {
                validate(ValidationData::value).isValidIdentitetsnummer()
            }
        }
    }

    @Test
    fun isValidOrganisasjonsnummer_OK() {
        validate(ValidationData(TestData.validOrgNr)) {
            validate(ValidationData::value).isValidOrganisasjonsnummer()
        }
    }

    @Test
    fun isValidOrganisasjonsnummer_Invalid() {
        assertThrows<ConstraintViolationException> {
            validate(ValidationData(TestData.notValidOrgNr)) {
                validate(ValidationData::value).isValidOrganisasjonsnummer()
            }
        }
    }
}