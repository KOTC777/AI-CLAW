#!/usr/bin/env python3
"""Send email via SMTP with support for HTML, attachments, CC/BCC."""

import argparse
import json
import os
import sys
import smtplib
import mimetypes
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.mime.base import MIMEBase
from email import encoders
from email.utils import formataddr
from pathlib import Path


def load_config(config_path=None):
    if config_path:
        p = Path(config_path)
    else:
        p = Path(__file__).resolve().parent.parent / "config.json"
    if not p.exists():
        print(f"Error: config not found at {p}", file=sys.stderr)
        sys.exit(1)
    with open(p) as f:
        cfg = json.load(f)
    required = ["smtp_host", "smtp_port", "username", "password", "from_email"]
    missing = [k for k in required if not cfg.get(k)]
    if missing:
        print(f"Error: missing config fields: {', '.join(missing)}", file=sys.stderr)
        sys.exit(1)
    return cfg


def parse_recipients(s):
    if not s:
        return []
    return [r.strip() for r in s.split(",") if r.strip()]


def add_attachment(msg, filepath):
    p = Path(filepath)
    if not p.exists():
        print(f"Warning: attachment not found: {filepath}", file=sys.stderr)
        return
    ctype, encoding = mimetypes.guess_type(str(p))
    if ctype is None or encoding is not None:
        ctype = "application/octet-stream"
    maintype, subtype = ctype.split("/", 1)
    with open(p, "rb") as f:
        part = MIMEBase(maintype, subtype)
        part.set_payload(f.read())
    encoders.encode_base64(part)
    part.add_header("Content-Disposition", "attachment", filename=p.name)
    msg.attach(part)


def send_email(cfg, to, subject, body, cc=None, bcc=None, html=False, attachments=None):
    msg = MIMEMultipart()
    from_name = cfg.get("from_name", "")
    msg["From"] = formataddr((from_name, cfg["from_email"])) if from_name else cfg["from_email"]
    msg["To"] = ", ".join(to)
    if cc:
        msg["Cc"] = ", ".join(cc)
    msg["Subject"] = subject

    content_type = "html" if html else "plain"
    msg.attach(MIMEText(body, content_type, "utf-8"))

    if attachments:
        for fp in attachments:
            add_attachment(msg, fp)

    all_recipients = to + (cc or []) + (bcc or [])

    use_ssl = cfg.get("smtp_ssl", True)
    host = cfg["smtp_host"]
    port = cfg["smtp_port"]

    try:
        if use_ssl:
            server = smtplib.SMTP_SSL(host, port, timeout=30)
        else:
            server = smtplib.SMTP(host, port, timeout=30)
            server.starttls()
        server.login(cfg["username"], cfg["password"])
        server.sendmail(cfg["from_email"], all_recipients, msg.as_string())
        server.quit()
        print(f"Email sent successfully to: {', '.join(all_recipients)}")
    except Exception as e:
        print(f"Error sending email: {e}", file=sys.stderr)
        sys.exit(1)


def main():
    parser = argparse.ArgumentParser(description="Send email via SMTP")
    parser.add_argument("--to", required=True, help="Recipient(s), comma-separated")
    parser.add_argument("--subject", required=True, help="Email subject")
    parser.add_argument("--body", required=True, help="Email body")
    parser.add_argument("--cc", help="CC recipient(s), comma-separated")
    parser.add_argument("--bcc", help="BCC recipient(s), comma-separated")
    parser.add_argument("--html", action="store_true", help="Treat body as HTML")
    parser.add_argument("--attach", help="Attachment file path(s), comma-separated")
    parser.add_argument("--config", help="Path to config.json")
    args = parser.parse_args()

    cfg = load_config(args.config)
    to = parse_recipients(args.to)
    cc = parse_recipients(args.cc)
    bcc = parse_recipients(args.bcc)
    attachments = parse_recipients(args.attach) if args.attach else None

    if not to:
        print("Error: no valid recipients in --to", file=sys.stderr)
        sys.exit(1)

    send_email(cfg, to, args.subject, args.body, cc, bcc, args.html, attachments)


if __name__ == "__main__":
    main()
