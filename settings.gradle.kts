plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "flick-server"

include("common")
include("service-registry")
include("api-gateway")
include(
    "admin-service",
    "place-service",
    "core-service",
    "notification-service",
)
include(
    "booth-domain",
    "user-domain",
    "product-domain",
    "notification-domain",
    "order-domain",
    "transaction-domain",
    "payment-domain",
    "kiosk-domain",
    "notice-domain",
)