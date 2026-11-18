package com.example.bip39onboarding.risk

/**
 * Optional counterparty / self-exposure hygiene (Stigmata-ready).
 * No KYC — protocol risk signals only.
 */
fun interface AddressRiskProvider {
    suspend fun assess(address: String, direction: Direction): RiskReport
}

enum class Direction {
    Outbound,
    InboundSelf,
}

enum class RiskLevel {
    Clean,
    Warn,
    Review,
    Block,
}

data class RiskReport(
    val address: String,
    val level: RiskLevel,
    val score: Int,
    val summary: String,
    val flags: List<String> = emptyList(),
)

/** Always-clean stub until Stigmata / local AML is wired. */
object NoopAddressRiskProvider : AddressRiskProvider {
    override suspend fun assess(address: String, direction: Direction): RiskReport =
        RiskReport(
            address = address,
            level = RiskLevel.Clean,
            score = 0,
            summary = "No risk provider configured",
        )
}
