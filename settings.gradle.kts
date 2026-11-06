rootProject.name = "bank-microservice-app"

include(
    "bank-ui",
    "bank-gateway"
)

include("transfer-service")
include("account-service")
include("cash-service")
include("notification-service")