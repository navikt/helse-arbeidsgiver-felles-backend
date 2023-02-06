package no.nav.helse.arbeidsgiver.integrasjoner.sts

import io.ktor.client.features.*
import io.ktor.http.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.io.File
import java.util.Base64

class RestSTSAccessTokenProviderTest {

    @Test
    internal fun `valid answer from STS returns valid token, second call gives cached answer`() {
        val token = buildClient(HttpStatusCode.OK, validStsResponse).getToken()
        assertThat(token).isNotNull()
        val token2 = buildClient(HttpStatusCode.OK, validStsResponse).getToken()
        assertThat(token).isEqualTo(token2)
    }

    @Test
    internal fun `Error response (5xx) from STS throws exception`() {
        assertThrows(ServerResponseException::class.java) {
            buildClient(HttpStatusCode.InternalServerError, "")
        }
    }

    @Test
    internal fun `Test token decode`() {
        val base64String = "PHNhbWwyOkFzc2VydGlvbiB4bWxuczpzYW1sMj0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmFzc2VydGlvbiIgSUQ9IlNBTUwtYzYxNTEzMjItN2JiNS00YjU0LWI2NWMtNDM1Nzk3OGRlOTU3IiBJc3N1ZUluc3RhbnQ9IjIwMjMtMDEtMDRUMTM6MjM6MjIuMzQ3WiIgVmVyc2lvbj0iMi4wIj48c2FtbDI6SXNzdWVyPklTMDI8L3NhbWwyOklzc3Vlcj48U2lnbmF0dXJlIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjIj48U2lnbmVkSW5mbz48Q2Fub25pY2FsaXphdGlvbk1ldGhvZCBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjxTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjcnNhLXNoYTEiLz48UmVmZXJlbmNlIFVSST0iI1NBTUwtYzYxNTEzMjItN2JiNS00YjU0LWI2NWMtNDM1Nzk3OGRlOTU3Ij48VHJhbnNmb3Jtcz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8-PC9UcmFuc2Zvcm1zPjxEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwLzA5L3htbGRzaWcjc2hhMSIvPjxEaWdlc3RWYWx1ZT5OcnVRb1p4dHlzUUZveUhhSHdGaTQ4emJGTGc9PC9EaWdlc3RWYWx1ZT48L1JlZmVyZW5jZT48L1NpZ25lZEluZm8-PFNpZ25hdHVyZVZhbHVlPkw0VEJGUHpTK2d6bVU0eWI0UGZmU28zc3ZZZ1hzSGljdVRyNnpRVEx1NStPSHA2eDBEK3FWMFZVSXFRK2RJMk4yWVRLaHNxdHQ4WnImIzEzOwpmb0ZIYllhb1VXdnZ2anVQbW45R1AvUGlrK0pPYUFYZTFpSUYvZFRvQVl6WlVhSVhFTnpDQ0RKOUY2QmlRL2NmTWUrR2FhLzYyQ05YJiMxMzsKNm9HaDM1UWpIaUJvdWltak9YdGtncHdaUFRrdHBwWlozazFySUlWbWRmZVlVZDl2cWFERWFnZC9ZaG95M3N0ZTRtakZOdHpsYVNZVCYjMTM7CndlTXF1WUpmUzMyc3RFR3VpZWIzY3pZZEpDSUVGYjg0OXFqK3g3UmNmSkh6YjJyUnZzNlRWY3pnOWdaSjJUSy90dDJacFArOHVXdE4mIzEzOwphbkoweFV0dHdtKzUrMHhOcUY4VTBST2o4cVFzblhaVjh2S1FtUT09PC9TaWduYXR1cmVWYWx1ZT48S2V5SW5mbz48WDUwOURhdGE-PFg1MDlDZXJ0aWZpY2F0ZT5NSUlHc3pDQ0JadWdBd0lCQWdJVGFnQUFRUWFpYVR6SUhYdytvd0FCQUFCQkJqQU5CZ2txaGtpRzl3MEJBUXNGQURCUU1SVXdFd1lLJiMxMzsKQ1pJbWlaUHlMR1FCR1JZRmJHOWpZV3d4RnpBVkJnb0praWFKay9Jc1pBRVpGZ2R3Y21Wd2NtOWtNUjR3SEFZRFZRUURFeFZDTWpjZyYjMTM7ClNYTnpkV2x1WnlCRFFTQkpiblJsY200d0hoY05NakV3TVRFeE1URXdOekEzV2hjTk1qTXdNVEV4TVRFeE56QTNXakJUTVFzd0NRWUQmIzEzOwpWUVFHRXdKT1R6RU5NQXNHQTFVRUNCTUVUMU5NVHpFTk1Bc0dBMVVFQnhNRVQxTk1UekVNTUFvR0ExVUVDaE1EVGtGV01SZ3dGZ1lEJiMxMzsKVlFRRERBOHFMbkJ5WlhCeWIyUXViRzlqWVd3d2dnRWlNQTBHQ1NxR1NJYjNEUUVCQVFVQUE0SUJEd0F3Z2dFS0FvSUJBUUNwK2R2YyYjMTM7ClU5NkxhaE9tZTRTR2xYbmVSbmNjZGR5SG55MlFHTVlSVnNPa1djcG9aMHNjTnI3ME1EM0VLNzAwWE1BU1VvU2cwb0lORUxWTW5XbWEmIzEzOwpBcys3QXQvOXluSmtZWTU2ZHIxWE1qSEVOSnIxdE5PYkpVZi9XSG84UGUwY3dnRlhTTkxqR1k5eGlGUnQxRWQyQTg5OEo2aWRTUUN1JiMxMzsKcm5NQkUzQWVRU3NwcmJQL3Q1akJYV21Ec0QwSkNMZzRmbDB5L0FHMnhjRkhaR0xXV1lBR2lqSTVYYXFFZUpmeWpGYmVRRVBOM1MzQiYjMTM7ClhmZkFud0hMZmNJNXlMU1JiNUE2Yk5hbDJBSHZzN2VnSVE0K0UwSVhFQ2lQcVNCR3ROenVGQi9xV0FWT1lOQzZVeER4K01BRGtVNGEmIzEzOwpKdmhsMHBSamhDVzFNdjRmVGYvT1ZVVzdKR0VMS01ZSEFnTUJBQUdqZ2dPQk1JSURmVEFhQmdOVkhSRUVFekFSZ2c4cUxuQnlaWEJ5JiMxMzsKYjJRdWJHOWpZV3d3SFFZRFZSME9CQllFRk8zYW54bzFzRFJBWHBna0p1RkR5STdCaWp1NE1COEdBMVVkSXdRWU1CYUFGT05vWTFXOSYjMTM7CjIyamJOelhrWUtsU0I2c2dtcXVOTUlJQklRWURWUjBmQklJQkdEQ0NBUlF3Z2dFUW9JSUJES0NDQVFpR2djZHNaR0Z3T2k4dkwyTnUmIzEzOwpQVUl5TnlVeU1FbHpjM1ZwYm1jbE1qQkRRU1V5TUVsdWRHVnliaXhEVGoxQ01qZEVVbFpYTURBNExFTk9QVU5FVUN4RFRqMVFkV0pzJiMxMzsKYVdNbE1qQnJaWGtsTWpCVFpYSjJhV05sY3l4RFRqMVRaWEoyYVdObGN5eERUajFEYjI1bWFXZDFjbUYwYVc5dUxFUkRQWEJ5WlhCeSYjMTM7CmIyUXNSRU05Ykc5allXdy9ZMlZ5ZEdsbWFXTmhkR1ZTWlhadlkyRjBhVzl1VEdsemREOWlZWE5sUDI5aWFtVmpkRU5zWVhOelBXTlMmIzEzOwpURVJwYzNSeWFXSjFkR2x2YmxCdmFXNTBoanhvZEhSd09pOHZZM0pzTG5CeVpYQnliMlF1Ykc5allXd3ZRM0pzTDBJeU55VXlNRWx6JiMxMzsKYzNWcGJtY2xNakJEUVNVeU1FbHVkR1Z5Ymk1amNtd3dnZ0ZqQmdnckJnRUZCUWNCQVFTQ0FWVXdnZ0ZSTUlHOEJnZ3JCZ0VGQlFjdyYjMTM7CkFvYUJyMnhrWVhBNkx5OHZZMjQ5UWpJM0pUSXdTWE56ZFdsdVp5VXlNRU5CSlRJd1NXNTBaWEp1TEVOT1BVRkpRU3hEVGoxUWRXSnMmIzEzOwphV01sTWpCclpYa2xNakJUWlhKMmFXTmxjeXhEVGoxVFpYSjJhV05sY3l4RFRqMURiMjVtYVdkMWNtRjBhVzl1TEVSRFBYQnlaWEJ5JiMxMzsKYjJRc1JFTTliRzlqWVd3L1kwRkRaWEowYVdacFkyRjBaVDlpWVhObFAyOWlhbVZqZEVOc1lYTnpQV05sY25ScFptbGpZWFJwYjI1QiYjMTM7CmRYUm9iM0pwZEhrd0tnWUlLd1lCQlFVSE1BR0dIbWgwZEhBNkx5OXZZM053TG5CeVpYQnliMlF1Ykc5allXd3ZiMk56Y0RCa0JnZ3ImIzEzOwpCZ0VGQlFjd0FvWllhSFIwY0RvdkwyTnliQzV3Y21Wd2NtOWtMbXh2WTJGc0wwTnliQzlDTWpkRVVsWlhNREE0TG5CeVpYQnliMlF1JiMxMzsKYkc5allXeGZRakkzSlRJd1NYTnpkV2x1WnlVeU1FTkJKVEl3U1c1MFpYSnVLREVwTG1OeWREQU9CZ05WSFE4QkFmOEVCQU1DQmFBdyYjMTM7Ck93WUpLd1lCQkFHQ054VUhCQzR3TEFZa0t3WUJCQUdDTnhVSWdkYlZYSU9BcDF5RTlaMGttNlJUb0xKNWdTU0h2cDFGa1lNaUFnRmsmIzEzOwpBZ0VDTUIwR0ExVWRKUVFXTUJRR0NDc0dBUVVGQndNQkJnZ3JCZ0VGQlFjREFqQW5CZ2tyQmdFRUFZSTNGUW9FR2pBWU1Bb0dDQ3NHJiMxMzsKQVFVRkJ3TUJNQW9HQ0NzR0FRVUZCd01DTUEwR0NTcUdTSWIzRFFFQkN3VUFBNElCQVFBbkdWbnBDWXhHSlZuaEZGWDRJMVZ5N2Y4OCYjMTM7CjN5TGh4dXVUS2QxWlUrcXBJYjBxM2U0VWxIcUlwS1J6SWp0V2l5cXdrUmtVcXFRVHFTa2ttL1V2emJWZGpOQTAyOUMzanFhdXNYcGMmIzEzOwpGaHpnUWZ3MjRpTnhjZkRKeU5DWnlhK1JXQk9tdWwwQ1ZEczh6SzRQUndMc1lsTTVibnF2TmJteVZaZXYrV1VCTFFMTzRsNFVxTjZTJiMxMzsKcThqSVlCRlVYbWpRVG11Z0JobERLYXZ6dGduZ1BKbkcxQ083NkhTWGIwWGZTYWhmc3E4akFRZGpPVlBRSVNkSXkraGtia3Y1eVhicyYjMTM7ClJjNVBjY29DdzZsMXBXK1FLckl3UUttejYzTzZxTldPQTVkQzkyWllWRitOM0pnMTJOK25hblowNlV3aGVQMHluTkc1T0pXdTBtQVgmIzEzOwo0Ym4vZzBnNFk4ZXA8L1g1MDlDZXJ0aWZpY2F0ZT48WDUwOUlzc3VlclNlcmlhbD48WDUwOUlzc3Vlck5hbWU-Q049QjI3IElzc3VpbmcgQ0EgSW50ZXJuLCBEQz1wcmVwcm9kLCBEQz1sb2NhbDwvWDUwOUlzc3Vlck5hbWU-PFg1MDlTZXJpYWxOdW1iZXI-MjM2Mzg3OTA3NzQ3ODUxMzY0ODI5NzQwNzQwMzUyNDg0NDU2NjYyODgxMTAxNDwvWDUwOVNlcmlhbE51bWJlcj48L1g1MDlJc3N1ZXJTZXJpYWw-PC9YNTA5RGF0YT48L0tleUluZm8-PC9TaWduYXR1cmU-PHNhbWwyOlN1YmplY3Q-PHNhbWwyOk5hbWVJRCBGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjEuMTpuYW1laWQtZm9ybWF0OnVuc3BlY2lmaWVkIj5aOTkwNjgyPC9zYW1sMjpOYW1lSUQ-PHNhbWwyOlN1YmplY3RDb25maXJtYXRpb24gTWV0aG9kPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6Y206YmVhcmVyIj48c2FtbDI6U3ViamVjdENvbmZpcm1hdGlvbkRhdGEgTm90QmVmb3JlPSIyMDIzLTAxLTA0VDEzOjIzOjIyLjM0N1oiIE5vdE9uT3JBZnRlcj0iMjAyMy0wMS0wNFQxMzo1Nzo0OC4zNDdaIi8-PC9zYW1sMjpTdWJqZWN0Q29uZmlybWF0aW9uPjwvc2FtbDI6U3ViamVjdD48c2FtbDI6Q29uZGl0aW9ucyBOb3RCZWZvcmU9IjIwMjMtMDEtMDRUMTM6MjM6MjIuMzQ3WiIgTm90T25PckFmdGVyPSIyMDIzLTAxLTA0VDEzOjU3OjQ4LjM0N1oiLz48c2FtbDI6QXR0cmlidXRlU3RhdGVtZW50PjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iaWRlbnRUeXBlIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI-PHNhbWwyOkF0dHJpYnV0ZVZhbHVlPkludGVybkJydWtlcjwvc2FtbDI6QXR0cmlidXRlVmFsdWU-PC9zYW1sMjpBdHRyaWJ1dGU-PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJhdXRoZW50aWNhdGlvbkxldmVsIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OnVyaSI-PHNhbWwyOkF0dHJpYnV0ZVZhbHVlPjQ8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjxzYW1sMjpBdHRyaWJ1dGUgTmFtZT0iY29uc3VtZXJJZCIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDp1cmkiPjxzYW1sMjpBdHRyaWJ1dGVWYWx1ZT5zcnZzcGJlcmVnbmluZzwvc2FtbDI6QXR0cmlidXRlVmFsdWU-PC9zYW1sMjpBdHRyaWJ1dGU-PHNhbWwyOkF0dHJpYnV0ZSBOYW1lPSJhdWRpdFRyYWNraW5nSWQiIE5hbWVGb3JtYXQ9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphdHRybmFtZS1mb3JtYXQ6dXJpIj48c2FtbDI6QXR0cmlidXRlVmFsdWU-NDczYjZiMzYtYjEyNC00ZGE2LThhZGItZjBlY2QwYThmZmViLTE5MzgzOTQ8L3NhbWwyOkF0dHJpYnV0ZVZhbHVlPjwvc2FtbDI6QXR0cmlidXRlPjwvc2FtbDI6QXR0cmlidXRlU3RhdGVtZW50Pjwvc2FtbDI6QXNzZXJ0aW9uPg"
        val bytes = Base64.getUrlDecoder().decode(base64String)
        val file = File("decodedstring")
        val string = String(bytes, Charsets.UTF_8)

        val writer = file.printWriter()
        writer.println(string)
        writer.close()

        //  file.writeBytes(String(bytes).encodeToByteArray())
    }
}
