---
description: Build and deploy movie-api to the dev server by running deploy-dev/dev.sh
---

Run the project's dev deploy script.

Steps:
1. Run this via the Bash tool from the repo root (`movie-api/`):
   ```
   cd deploy-dev && bash dev.sh
   ```
   This script builds the Maven project, packages it, uploads it over SSH to `root@192.168.102.6`, and restarts the `movie-api` systemd service — it takes a few minutes and needs a generous timeout (use at least 300000ms, or run in background and monitor).
2. Do not swallow errors: `dev.sh` has no `set -e`, so it keeps running later steps even if an earlier step (e.g. `mvn clean package`) fails. Watch the full output — a Maven `BUILD FAILURE` or `ssh`/`scp` error earlier in the log means the deploy did not actually succeed, even if the script prints `############# DONE #############` at the end.
3. Report back clearly: whether the Maven build succeeded, whether the upload/SSH steps succeeded, and whether the service was restarted, based on what actually appeared in the output — not just the final banner.
