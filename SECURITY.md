# Security Policy

This is an educational project. Do not use it with real customer data or production credentials.

## Reporting a vulnerability

Do not publish credentials, exploit details, personal data, or a live vulnerability in a public issue.

Use GitHub's private vulnerability reporting feature if it is enabled. If it is unavailable, contact the repository owner through the GitHub profile and share only the minimum information needed for private coordination.

## Secrets

- Use environment variables or a secret manager for credentials.
- Never commit passwords, API tokens, private keys, database dumps, or personal data.
- Treat credentials committed to Git as compromised even after removal from the current branch.
- Rotate exposed credentials at the issuing system before considering history cleanup.
- Coordinate history rewrites with contributors because existing clones and branches may need to be recreated.
