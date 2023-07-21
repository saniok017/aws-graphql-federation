package com.github.arhor.dgs.users.service.impl

import com.github.arhor.dgs.users.service.TokenProvider
import io.jsonwebtoken.JwtBuilder
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.*
import kotlin.time.Duration

@Component
class JwtTokenProviderImpl(@Value("\${app-props.jwt.expire}") expire: String) : TokenProvider {

    private val jwtExpiration = Duration.parse(expire).inWholeMilliseconds
    private val jwtSigningKey = Keys.keyPairFor(SignatureAlgorithm.RS512)

    override fun createSignedJwt(customize: JwtBuilder.() -> Unit): String {
        val dateFrom = System.currentTimeMillis()
        val dateTill = dateFrom + jwtExpiration

        return Jwts.builder()
            .setIssuedAt(Date(dateFrom))
            .setExpiration(Date(dateTill))
            .apply(customize)
            .signWith(jwtSigningKey.private)
            .compact()
    }

    override fun activePublicKey(): String = """
        -----BEGIN PUBLIC KEY-----
        ${Base64.getEncoder().encodeToString(jwtSigningKey.public.encoded)}
        -----END PUBLIC KEY-----
        """.trimIndent()
}
