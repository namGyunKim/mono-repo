rootProject.name = "mono-repo"

include("apps:user-api")
include("apps:admin-api")
include("libs:backend:common")
include("libs:backend:global-core")
include("libs:backend:domain-core")
include("libs:backend:security-web")
include("libs:backend:web-support")
