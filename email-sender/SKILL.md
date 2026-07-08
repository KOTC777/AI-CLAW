---
name: email-sender
description: Send emails via SMTP. Use when the user asks to send an email, compose a message to someone's inbox, or deliver a report/notification by email. Supports plain text, HTML, attachments, CC/BCC, and multiple recipients.
---

# Email Sender

Send emails through SMTP using the bundled Python script.

## Configuration

Before first use, fill in SMTP credentials in `config.json` (same directory as this SKILL.md):

```json
{
  "smtp_host": "smtp.example.com",
  "smtp_port": 465,
  "smtp_ssl": true,
  "username": "your-email@example.com",
  "password": "your-app-password",
  "from_name": "Your Name",
  "from_email": "your-email@example.com"
}
```

- `smtp_ssl`: `true` for SSL (port 465), `false` for STARTTLS (port 587)
- `password`: use an app-specific password if your provider supports it (Gmail, QQ Mail, etc.)

## Usage

```bash
python3 scripts/send_email.py \
  --to "recipient@example.com" \
  --subject "Subject line" \
  --body "Email body text or HTML"
```

### Options

| Flag | Description |
|------|-------------|
| `--to` | Recipient(s), comma-separated |
| `--cc` | CC recipient(s), comma-separated |
| `--bcc` | BCC recipient(s), comma-separated |
| `--subject` | Email subject |
| `--body` | Body content (plain text or HTML) |
| `--html` | Treat body as HTML |
| `--attach` | File path(s) to attach, comma-separated |
| `--config` | Path to config.json (default: same dir as script) |

### Examples

**Plain text:**
```bash
python3 scripts/send_email.py \
  --to "alice@example.com" \
  --subject "Hello" \
  --body "Just checking in."
```

**HTML with attachment:**
```bash
python3 scripts/send_email.py \
  --to "alice@example.com,bob@example.com" \
  --subject "Report" \
  --body "<h1>Monthly Report</h1><p>See attached.</p>" \
  --html \
  --attach "./report.pdf"
```

**With CC:**
```bash
python3 scripts/send_email.py \
  --to "alice@example.com" \
  --cc "bob@example.com" \
  --subject "FYI" \
  --body "Please review."
```

## Notes

- The script reads `config.json` from its own directory by default. Use `--config` to override.
- Keep `config.json` out of version control — it contains credentials.
- For large attachments, check your SMTP provider's size limits (typically 25MB).
