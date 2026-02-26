rootProject.name = "mono-repo"

include("apps:user-api")
include("apps:admin-api")
include("libs:backend:global-core")
include("libs:backend:global-domain")
include("libs:backend:security-domain")
include("libs:backend:web-support")
