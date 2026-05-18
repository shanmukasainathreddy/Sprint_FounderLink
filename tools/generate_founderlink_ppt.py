from __future__ import annotations

import math
import shutil
import zipfile
from pathlib import Path
from xml.sax.saxutils import escape

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "FounderLink_Project_Presentation.pptx"
PREVIEW_DIR = ROOT / "docs" / "ppt_previews"
BUILD = ROOT / "docs" / ".pptx_build"

SLIDE_W = 13.333333
SLIDE_H = 7.5
EMU = 914400

NS = {
    "a": "http://schemas.openxmlformats.org/drawingml/2006/main",
    "r": "http://schemas.openxmlformats.org/officeDocument/2006/relationships",
    "p": "http://schemas.openxmlformats.org/presentationml/2006/main",
}


def emu(value: float) -> int:
    return int(value * EMU)


def xml_escape(value: str) -> str:
    return escape(value, {"\n": "&#10;"})


def color(hex_value: str) -> str:
    return hex_value.replace("#", "").upper()


def text_runs(text: str, size: int, fill: str, bold: bool = False) -> str:
    attrs = f' lang="en-US" sz="{size * 100}"'
    if bold:
        attrs += ' b="1"'
    return (
        f"<a:r><a:rPr{attrs}><a:solidFill><a:srgbClr val=\"{color(fill)}\"/>"
        f"</a:solidFill><a:latin typeface=\"Aptos\"/></a:rPr><a:t>{xml_escape(text)}</a:t></a:r>"
    )


def textbox(
    idx: int,
    name: str,
    x: float,
    y: float,
    w: float,
    h: float,
    text: str,
    size: int = 22,
    fill: str = "#17202A",
    bold: bool = False,
    align: str = "l",
) -> str:
    paragraphs = []
    for line in text.split("\n"):
        paragraphs.append(
            f'<a:p><a:pPr algn="{align}"/>'
            f"{text_runs(line, size, fill, bold)}"
            "</a:p>"
        )
    return f"""
<p:sp>
  <p:nvSpPr><p:cNvPr id="{idx}" name="{xml_escape(name)}"/><p:cNvSpPr txBox="1"/><p:nvPr/></p:nvSpPr>
  <p:spPr><a:xfrm><a:off x="{emu(x)}" y="{emu(y)}"/><a:ext cx="{emu(w)}" cy="{emu(h)}"/></a:xfrm><a:prstGeom prst="rect"><a:avLst/></a:prstGeom><a:noFill/><a:ln><a:noFill/></a:ln></p:spPr>
  <p:txBody><a:bodyPr wrap="square" lIns="0" tIns="0" rIns="0" bIns="0"><a:spAutoFit/></a:bodyPr><a:lstStyle/>{''.join(paragraphs)}</p:txBody>
</p:sp>"""


def rect(
    idx: int,
    name: str,
    x: float,
    y: float,
    w: float,
    h: float,
    fill: str,
    line: str = "#FFFFFF",
    radius: str = "roundRect",
) -> str:
    return f"""
<p:sp>
  <p:nvSpPr><p:cNvPr id="{idx}" name="{xml_escape(name)}"/><p:cNvSpPr/><p:nvPr/></p:nvSpPr>
  <p:spPr><a:xfrm><a:off x="{emu(x)}" y="{emu(y)}"/><a:ext cx="{emu(w)}" cy="{emu(h)}"/></a:xfrm><a:prstGeom prst="{radius}"><a:avLst/></a:prstGeom><a:solidFill><a:srgbClr val="{color(fill)}"/></a:solidFill><a:ln w="9525"><a:solidFill><a:srgbClr val="{color(line)}"/></a:solidFill></a:ln></p:spPr>
</p:sp>"""


def line(idx: int, name: str, x1: float, y1: float, x2: float, y2: float, fill: str = "#5B6472", width: int = 19050) -> str:
    x = min(x1, x2)
    y = min(y1, y2)
    w = abs(x2 - x1) or 0.01
    h = abs(y2 - y1) or 0.01
    flip_h = ' flipH="1"' if x2 < x1 else ""
    flip_v = ' flipV="1"' if y2 < y1 else ""
    return f"""
<p:cxnSp>
  <p:nvCxnSpPr><p:cNvPr id="{idx}" name="{xml_escape(name)}"/><p:cNvCxnSpPr/><p:nvPr/></p:nvCxnSpPr>
  <p:spPr><a:xfrm{flip_h}{flip_v}><a:off x="{emu(x)}" y="{emu(y)}"/><a:ext cx="{emu(w)}" cy="{emu(h)}"/></a:xfrm><a:prstGeom prst="line"><a:avLst/></a:prstGeom><a:ln w="{width}"><a:solidFill><a:srgbClr val="{color(fill)}"/></a:solidFill><a:tailEnd type="triangle"/></a:ln></p:spPr>
</p:cxnSp>"""


def table_box(idx_start: int, x: float, y: float, title: str, rows: list[str], accent: str) -> tuple[str, int]:
    parts = [
        rect(idx_start, f"{title} box", x, y, 2.15, 1.15 + 0.23 * len(rows), "#FFFFFF", "#D4DAE2", "rect"),
        rect(idx_start + 1, f"{title} head", x, y, 2.15, 0.38, accent, accent, "rect"),
        textbox(idx_start + 2, f"{title} title", x + 0.12, y + 0.08, 1.9, 0.25, title, 11, "#FFFFFF", True),
    ]
    n = idx_start + 3
    for i, row in enumerate(rows):
        parts.append(textbox(n, f"{title} row {i}", x + 0.12, y + 0.48 + i * 0.25, 1.9, 0.2, row, 8, "#27313F"))
        n += 1
    return "".join(parts), n


def slide_xml(shapes: list[str], bg: str = "#F6F7F9") -> str:
    return f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<p:sld xmlns:a="{NS["a"]}" xmlns:r="{NS["r"]}" xmlns:p="{NS["p"]}">
  <p:cSld><p:bg><p:bgPr><a:solidFill><a:srgbClr val="{color(bg)}"/></a:solidFill><a:effectLst/></p:bgPr></p:bg>
    <p:spTree>
      <p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr>
      <p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr>
      {''.join(shapes)}
    </p:spTree>
  </p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr>
</p:sld>'''


def base_title(title: str, kicker: str, n: int) -> list[str]:
    return [
        textbox(10, f"slide {n} kicker", 0.55, 0.25, 4.5, 0.25, kicker.upper(), 10, "#607089", True),
        textbox(11, f"slide {n} title", 0.55, 0.52, 9.2, 0.55, title, 25, "#17202A", True),
        textbox(12, f"slide {n} number", 12.25, 6.95, 0.45, 0.2, f"{n:02}", 9, "#8A95A6", True, "r"),
    ]


def build_slides() -> list[str]:
    slides = []
    s = [
        rect(2, "left field", 0, 0, 5.15, SLIDE_H, "#0B1F33", "#0B1F33", "rect"),
        rect(3, "signal bar", 5.15, 0, 0.18, SLIDE_H, "#20B486", "#20B486", "rect"),
        textbox(4, "cover label", 0.65, 0.7, 3.8, 0.35, "MICROSERVICES PROJECT", 12, "#9EE8CE", True),
        textbox(5, "cover title", 0.65, 1.45, 4.0, 1.5, "FounderLink", 38, "#FFFFFF", True),
        textbox(6, "cover subtitle", 0.68, 3.05, 3.8, 0.9, "Connecting founders, investors, co-founders, admins, and notification workflows.", 18, "#DCE8F3"),
        textbox(7, "cover stack", 0.68, 5.85, 3.5, 0.55, "Angular + Spring Boot + Docker Compose", 14, "#9EE8CE", True),
        rect(8, "system canvas", 6.0, 0.75, 6.15, 5.75, "#FFFFFF", "#DEE5EF", "rect"),
        textbox(9, "canvas browser", 6.35, 1.1, 1.4, 0.35, "Browser", 14, "#17202A", True, "c"),
        rect(13, "node frontend", 6.2, 1.65, 1.8, 0.7, "#E9F7F2", "#20B486"),
        textbox(14, "node frontend label", 6.35, 1.86, 1.5, 0.25, "Angular UI", 12, "#0B5D46", True, "c"),
        rect(15, "node gateway", 8.55, 1.65, 1.8, 0.7, "#FFF0D8", "#F5A524"),
        textbox(16, "node gateway label", 8.7, 1.86, 1.5, 0.25, "API Gateway", 12, "#7A4B00", True, "c"),
        rect(17, "node services", 7.05, 3.0, 3.95, 1.65, "#EEF2FF", "#6B7CFF"),
        textbox(18, "node services label", 7.35, 3.2, 3.35, 0.8, "Auth  Users  Startups\nInvestments  Teams  Messages", 14, "#253189", True, "c"),
        rect(19, "node async", 7.2, 5.0, 1.65, 0.65, "#FDEEF5", "#D84B87"),
        textbox(20, "node async label", 7.35, 5.19, 1.35, 0.2, "RabbitMQ", 11, "#842D53", True, "c"),
        rect(21, "node data", 9.35, 5.0, 1.65, 0.65, "#EBF7FF", "#3498DB"),
        textbox(22, "node data label", 9.5, 5.19, 1.35, 0.2, "PostgreSQL", 11, "#145A86", True, "c"),
        line(23, "flow 1", 8.0, 2.0, 8.55, 2.0, "#68768A"),
        line(24, "flow 2", 9.45, 2.35, 9.0, 3.0, "#68768A"),
        line(25, "flow 3", 8.6, 4.65, 8.0, 5.0, "#68768A"),
        line(26, "flow 4", 9.3, 4.65, 10.0, 5.0, "#68768A"),
    ]
    slides.append(slide_xml(s, "#F5F7FA"))

    s = base_title("Problem Statement", "Why FounderLink", 2) + [
        textbox(20, "problem statement", 0.75, 1.35, 5.3, 1.25, "Early startup collaboration is fragmented across profiles, pitches, investments, team discovery, chats, and email follow-ups.", 25, "#17202A", True),
        textbox(21, "problem bullets", 0.8, 3.05, 4.6, 2.0, "• Founders need one place to publish and manage startup listings.\n• Investors need approved startup discovery and investment status tracking.\n• Co-founders need team invitations and role visibility.\n• Admins need review controls without touching service internals.", 18, "#303B4A"),
        rect(22, "outcome band", 6.6, 1.35, 5.4, 3.9, "#0B1F33", "#0B1F33", "rect"),
        textbox(23, "outcome title", 7.0, 1.75, 4.6, 0.4, "Project outcome", 18, "#9EE8CE", True),
        textbox(24, "outcome copy", 7.0, 2.35, 4.45, 1.75, "A role-based platform where each core business capability is isolated as a microservice and connected through a gateway, discovery, shared data stores, and async notifications.", 22, "#FFFFFF", True),
        textbox(25, "outcome note", 7.0, 4.55, 4.2, 0.3, "Single user journey, separate service ownership.", 14, "#C8D7E5"),
    ]
    slides.append(slide_xml(s))

    s = base_title("Microservices Deployment View", "Architecture", 3)
    coords = {
        "Angular\n:4200": (0.9, 1.6), "API Gateway\n:8080": (3.0, 1.6), "Config\n:8888": (5.25, 0.95), "Eureka\n:8761": (7.2, 0.95),
        "Auth\n:8081": (4.7, 2.5), "Users\n:8082": (6.45, 2.5), "Startups\n:8083": (8.2, 2.5), "Investments\n:8084": (9.95, 2.5),
        "Teams\n:8085": (4.7, 3.65), "Messages\n:8086": (6.45, 3.65), "Notifications\n:8087": (8.2, 3.65),
        "PostgreSQL\n:5432": (5.0, 5.15), "Redis\n:6379": (7.15, 5.15), "RabbitMQ\n:5672": (9.3, 5.15),
    }
    idx = 20
    for label, (x, y) in coords.items():
        fill = "#E9F7F2" if "Angular" in label else "#FFF0D8" if "Gateway" in label else "#EEF2FF" if ":" in label and any(k in label for k in ["Auth", "Users", "Startups", "Investments", "Teams", "Messages", "Notifications"]) else "#FFFFFF"
        s += [rect(idx, label + " node", x, y, 1.55, 0.72, fill, "#CBD5E1"), textbox(idx + 1, label + " text", x + 0.1, y + 0.14, 1.35, 0.35, label, 10, "#17202A", True, "c")]
        idx += 2
    s += [line(idx, "ui to gateway", 2.45, 1.95, 3.0, 1.95), line(idx + 1, "gateway to services", 4.25, 1.95, 5.45, 2.5), line(idx + 2, "services to db", 7.2, 4.37, 5.8, 5.15), line(idx + 3, "services to mq", 8.95, 4.37, 10.0, 5.15)]
    slides.append(slide_xml(s))

    s = base_title("ER Diagram", "Data model", 4)
    idx = 20
    boxes = [
        ("users", ["id PK", "email unique", "password", "enabled", "otp_code"], 0.7, 1.35, "#0B1F33"),
        ("roles", ["id PK", "name"], 3.05, 1.35, "#0B1F33"),
        ("user_roles", ["id PK", "user_id FK", "role_id FK"], 1.9, 3.05, "#20B486"),
        ("user_profiles", ["id PK", "name", "email", "skills", "location"], 5.25, 1.35, "#3498DB"),
        ("startups", ["id PK", "user_id founder", "title", "status", "funding_goal"], 7.65, 1.35, "#6B7CFF"),
        ("investments", ["id PK", "startup_id", "investor_id", "amount", "status"], 10.0, 1.35, "#F5A524"),
        ("teams", ["id PK", "startup_id", "user_id", "role", "status"], 7.65, 4.05, "#D84B87"),
        ("conversations", ["id PK", "user1_id", "user2_id"], 3.95, 4.65, "#607089"),
        ("messages", ["id PK", "conversation_id", "sender_id", "content", "timestamp"], 1.25, 5.0, "#607089"),
    ]
    for name, rows, x, y, accent in boxes:
        part, idx = table_box(idx, x, y, name, rows, accent)
        s.append(part)
    s += [
        line(idx, "users roles", 2.15, 2.2, 2.6, 3.05),
        line(idx + 1, "roles userroles", 3.55, 2.2, 3.25, 3.05),
        line(idx + 2, "profile startup", 7.4, 1.95, 7.65, 1.95),
        line(idx + 3, "startup investment", 9.8, 1.95, 10.0, 1.95),
        line(idx + 4, "startup team", 8.55, 3.25, 8.55, 4.05),
        line(idx + 5, "conversation messages", 3.95, 5.25, 3.4, 5.45),
    ]
    slides.append(slide_xml(s))

    s = base_title("Tech Stack", "Implementation", 5)
    stacks = [
        ("Frontend", "Angular 20\nTypeScript\nRouter, Forms, HttpClient\nKarma/Jasmine", "#20B486"),
        ("Backend", "Java 17\nSpring Boot\nSpring Security + JWT\nSpring Data JPA", "#6B7CFF"),
        ("Cloud Patterns", "Spring Cloud Gateway\nConfig Server\nEureka Discovery\nOpenFeign", "#F5A524"),
        ("Data & Events", "PostgreSQL\nRedis cache\nRabbitMQ\nSpring Mail", "#D84B87"),
        ("DevOps & Quality", "Docker Compose\nGitHub Actions\nJaCoCo coverage\nSonarQube", "#3498DB"),
    ]
    for i, (head, body, accent) in enumerate(stacks):
        x = 0.85 + (i % 3) * 3.85
        y = 1.45 + (i // 3) * 2.35
        s += [rect(30 + i * 3, head, x, y, 3.15, 1.55, "#FFFFFF", "#D8DEE8", "rect"), rect(31 + i * 3, head + " accent", x, y, 0.16, 1.55, accent, accent, "rect"), textbox(32 + i * 3, head + " text", x + 0.35, y + 0.22, 2.45, 1.05, f"{head}\n{body}", 13, "#17202A", True)]
    slides.append(slide_xml(s))

    endpoint_text = (
        "Auth: POST /auth/register, /verify-otp, /login, /forgot-password, /reset-password; GET /auth/user-roles\n"
        "Users: POST /users, GET /users, /users/directory, /users/{id}, /users/internal/{id}; PUT /users/{id}\n"
        "Startups: POST /startups, GET /startups, /startups/{id}; PUT /startups/{id}; DELETE /startups/{id}\n"
        "Investments: POST /investments, GET /investments, /startup/{id}, /investor/{id}; PUT /{id}/status\n"
        "Teams: POST /teams/invite, /teams/join; GET /teams/startup/{id}, /teams/my\n"
        "Messages: POST /messages/conversation, /messages; GET /messages/conversation/{id}, /messages/user/{id}/conversations\n"
        "Notifications: GET /notifications/status or /api/notifications/status"
    )
    s = base_title("API Endpoints", "Gateway surface", 6) + [
        textbox(20, "api intro", 0.75, 1.25, 11.2, 0.35, "Frontend calls the API Gateway at localhost:8080; the gateway routes to the owning service.", 16, "#3B4656"),
        rect(21, "api table", 0.75, 1.9, 11.65, 4.45, "#FFFFFF", "#D8DEE8", "rect"),
        textbox(22, "api list", 1.0, 2.18, 11.05, 3.75, endpoint_text, 11, "#17202A"),
    ]
    slides.append(slide_xml(s))

    services = [
        ("Gateway", "Single backend entry point and Swagger aggregation."),
        ("Auth", "Registration, OTP, login, JWT, password reset, roles."),
        ("Users", "Profiles, directory, internal summaries, Redis cache."),
        ("Startups", "Founder listings, discovery, admin review workflow."),
        ("Investments", "Investor requests and status lifecycle."),
        ("Teams", "Invites, joins, and startup team membership."),
        ("Messaging", "Conversations and stored user messages."),
        ("Notifications", "RabbitMQ consumer and email delivery."),
    ]
    s = base_title("Brief Description of Each Microservice", "Service ownership", 7)
    for i, (head, body) in enumerate(services):
        x = 0.75 + (i % 2) * 5.9
        y = 1.25 + (i // 2) * 1.28
        s += [textbox(30 + i * 2, head, x, y, 1.5, 0.25, head, 13, "#0B5D46", True), textbox(31 + i * 2, body, x + 1.45, y, 4.1, 0.55, body, 12, "#27313F")]
    slides.append(slide_xml(s))

    s = base_title("UI Components", "Angular frontend", 8) + [
        rect(20, "auth surface", 0.9, 1.25, 3.2, 4.7, "#FFFFFF", "#D8DEE8", "rect"),
        textbox(21, "auth title", 1.2, 1.55, 2.4, 0.35, "Auth Page", 18, "#17202A", True),
        textbox(22, "auth items", 1.2, 2.2, 2.4, 2.6, "Role selection\nRegistration steps\nOTP verification\nLogin\nPassword reset", 15, "#3B4656"),
        rect(23, "dash surface", 5.0, 1.25, 3.2, 4.7, "#FFFFFF", "#D8DEE8", "rect"),
        textbox(24, "dash title", 5.3, 1.55, 2.4, 0.35, "Dashboard", 18, "#17202A", True),
        textbox(25, "dash items", 5.3, 2.2, 2.4, 2.6, "Role-based service cards\nQuick navigation\nSession state\nLogout", 15, "#3B4656"),
        rect(26, "service surface", 9.1, 1.25, 3.2, 4.7, "#FFFFFF", "#D8DEE8", "rect"),
        textbox(27, "service title", 9.4, 1.55, 2.4, 0.35, "Service Page", 18, "#17202A", True),
        textbox(28, "service items", 9.4, 2.2, 2.4, 2.6, "Profiles\nStartups & discovery\nInvestments\nTeams\nMessages\nNotifications", 15, "#3B4656"),
    ]
    slides.append(slide_xml(s))

    s = base_title("UI Routing Endpoints", "Angular routes", 9) + [
        textbox(20, "routes main", 0.9, 1.4, 5.4, 0.8, "/auth\n/dashboard\n/services/:service", 30, "#17202A", True),
        textbox(21, "route notes", 0.95, 3.15, 4.9, 1.3, "Guest guard keeps logged-in users out of /auth.\nAuth guard protects dashboard and service pages.\nUnknown paths redirect back to /auth.", 17, "#3B4656"),
        rect(22, "service map", 7.0, 1.35, 4.5, 3.9, "#0B1F33", "#0B1F33", "rect"),
        textbox(23, "service map title", 7.35, 1.75, 3.8, 0.35, "Service keys", 17, "#9EE8CE", True),
        textbox(24, "service map body", 7.35, 2.35, 3.85, 2.05, "profiles · startups · discover\ninvestments · team · messages\nnotifications · admin", 21, "#FFFFFF", True, "c"),
    ]
    slides.append(slide_xml(s))

    s = base_title("Challenges Faced", "Build realities", 10) + [
        textbox(20, "challenges", 0.8, 1.35, 5.4, 4.2, "• Keeping JWT auth consistent across independent services.\n• Making services discoverable while still runnable through Docker Compose.\n• Coordinating PostgreSQL data ownership across separate service modules.\n• Decoupling email delivery from user-facing flows with RabbitMQ.\n• Maintaining coverage and SonarQube signals across many Maven projects plus Angular.", 18, "#27313F"),
        rect(21, "quality rail", 7.0, 1.35, 4.8, 3.7, "#FFFFFF", "#D8DEE8", "rect"),
        textbox(22, "quality title", 7.35, 1.7, 3.8, 0.35, "Quality controls", 19, "#17202A", True),
        textbox(23, "quality body", 7.35, 2.35, 3.8, 1.7, "Unit tests\nJaCoCo reports\nSonarQube scans\nGitHub Actions matrix\nDocker build validation", 17, "#3B4656"),
    ]
    slides.append(slide_xml(s))

    s = base_title("Learnings", "What the project taught", 11) + [
        textbox(20, "learn body", 0.9, 1.35, 5.2, 4.2, "Microservices are not just multiple folders. They require clear ownership, route contracts, discovery, configuration, authentication boundaries, test strategy, and failure-aware communication.", 26, "#17202A", True),
        textbox(21, "learn bullets", 7.0, 1.55, 4.6, 3.6, "• Gateway simplifies frontend integration.\n• Config and discovery reduce hardcoded coupling.\n• Async messaging improves responsiveness.\n• Role-based UI must mirror backend authorization.\n• CI/CD catches integration mistakes early.", 18, "#27313F"),
    ]
    slides.append(slide_xml(s))

    s = base_title("Future Enhancements", "Next iteration", 12) + [
        textbox(20, "future 1", 0.85, 1.35, 4.9, 3.8, "Short term\n• Real-time messaging with WebSocket/STOMP\n• Search, filters, and richer startup discovery\n• Notification inbox with read/unread state\n• Better admin moderation analytics", 18, "#17202A", True),
        textbox(21, "future 2", 7.0, 1.35, 4.9, 3.8, "Long term\n• Payment/investment document workflow\n• File upload for pitch decks\n• Kubernetes deployment manifests\n• Observability with logs, metrics, and tracing", 18, "#17202A", True),
        textbox(22, "closing", 1.0, 6.15, 10.8, 0.45, "FounderLink can evolve from a training project into a production-style startup collaboration platform.", 19, "#0B5D46", True, "c"),
    ]
    slides.append(slide_xml(s))
    return slides


def write_package(slides: list[str]) -> None:
    if BUILD.exists():
        shutil.rmtree(BUILD)
    (BUILD / "_rels").mkdir(parents=True)
    (BUILD / "docProps").mkdir()
    (BUILD / "ppt" / "_rels").mkdir(parents=True)
    (BUILD / "ppt" / "slides" / "_rels").mkdir(parents=True)
    (BUILD / "ppt" / "slideLayouts" / "_rels").mkdir(parents=True)
    (BUILD / "ppt" / "slideMasters" / "_rels").mkdir(parents=True)
    (BUILD / "ppt" / "theme").mkdir(parents=True)

    overrides = [
        '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>',
        '<Default Extension="xml" ContentType="application/xml"/>',
        '<Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>',
        '<Override PartName="/ppt/slideMasters/slideMaster1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideMaster+xml"/>',
        '<Override PartName="/ppt/slideLayouts/slideLayout1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slideLayout+xml"/>',
        '<Override PartName="/ppt/theme/theme1.xml" ContentType="application/vnd.openxmlformats-officedocument.theme+xml"/>',
        '<Override PartName="/docProps/core.xml" ContentType="application/vnd.openxmlformats-package.core-properties+xml"/>',
        '<Override PartName="/docProps/app.xml" ContentType="application/vnd.openxmlformats-officedocument.extended-properties+xml"/>',
    ]
    for i in range(1, len(slides) + 1):
        overrides.append(f'<Override PartName="/ppt/slides/slide{i}.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>')
    (BUILD / "[Content_Types].xml").write_text(f'<?xml version="1.0" encoding="UTF-8"?><Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">{"".join(overrides)}</Types>', encoding="utf-8")
    (BUILD / "_rels" / ".rels").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="ppt/presentation.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/package/2006/relationships/metadata/core-properties" Target="docProps/core.xml"/><Relationship Id="rId3" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/extended-properties" Target="docProps/app.xml"/></Relationships>''', encoding="utf-8")
    (BUILD / "docProps" / "core.xml").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><cp:coreProperties xmlns:cp="http://schemas.openxmlformats.org/package/2006/metadata/core-properties" xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:dcterms="http://purl.org/dc/terms/" xmlns:dcmitype="http://purl.org/dc/dcmitype/" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"><dc:title>FounderLink Project Presentation</dc:title><dc:creator>Codex</dc:creator><cp:lastModifiedBy>Codex</cp:lastModifiedBy></cp:coreProperties>''', encoding="utf-8")
    (BUILD / "docProps" / "app.xml").write_text(f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Properties xmlns="http://schemas.openxmlformats.org/officeDocument/2006/extended-properties" xmlns:vt="http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes"><Application>Codex</Application><PresentationFormat>Widescreen</PresentationFormat><Slides>{len(slides)}</Slides></Properties>''', encoding="utf-8")
    slide_ids = "".join([f'<p:sldId id="{255+i}" r:id="rId{i}"/>' for i in range(1, len(slides) + 1)])
    (BUILD / "ppt" / "presentation.xml").write_text(f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><p:presentation xmlns:a="{NS["a"]}" xmlns:r="{NS["r"]}" xmlns:p="{NS["p"]}"><p:sldMasterIdLst><p:sldMasterId id="2147483648" r:id="rId{len(slides)+1}"/></p:sldMasterIdLst><p:sldIdLst>{slide_ids}</p:sldIdLst><p:sldSz cx="{emu(SLIDE_W)}" cy="{emu(SLIDE_H)}" type="wide"/><p:notesSz cx="6858000" cy="9144000"/><p:defaultTextStyle/></p:presentation>''', encoding="utf-8")
    rels = [f'<Relationship Id="rId{i}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide" Target="slides/slide{i}.xml"/>' for i in range(1, len(slides) + 1)]
    rels.append(f'<Relationship Id="rId{len(slides)+1}" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="slideMasters/slideMaster1.xml"/>')
    (BUILD / "ppt" / "_rels" / "presentation.xml.rels").write_text(f'<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">{"".join(rels)}</Relationships>', encoding="utf-8")

    master = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><p:sldMaster xmlns:a="{NS["a"]}" xmlns:r="{NS["r"]}" xmlns:p="{NS["p"]}"><p:cSld><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMap bg1="lt1" tx1="dk1" bg2="lt2" tx2="dk2" accent1="accent1" accent2="accent2" accent3="accent3" accent4="accent4" accent5="accent5" accent6="accent6" hlink="hlink" folHlink="folHlink"/><p:sldLayoutIdLst><p:sldLayoutId id="2147483649" r:id="rId1"/></p:sldLayoutIdLst><p:txStyles><p:titleStyle/><p:bodyStyle/><p:otherStyle/></p:txStyles></p:sldMaster>'''
    (BUILD / "ppt" / "slideMasters" / "slideMaster1.xml").write_text(master, encoding="utf-8")
    (BUILD / "ppt" / "slideMasters" / "_rels" / "slideMaster1.xml.rels").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/><Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/theme" Target="../theme/theme1.xml"/></Relationships>''', encoding="utf-8")
    layout = f'''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><p:sldLayout xmlns:a="{NS["a"]}" xmlns:r="{NS["r"]}" xmlns:p="{NS["p"]}" type="blank" preserve="1"><p:cSld name="Blank"><p:spTree><p:nvGrpSpPr><p:cNvPr id="1" name=""/><p:cNvGrpSpPr/><p:nvPr/></p:nvGrpSpPr><p:grpSpPr><a:xfrm><a:off x="0" y="0"/><a:ext cx="0" cy="0"/><a:chOff x="0" y="0"/><a:chExt cx="0" cy="0"/></a:xfrm></p:grpSpPr></p:spTree></p:cSld><p:clrMapOvr><a:masterClrMapping/></p:clrMapOvr></p:sldLayout>'''
    (BUILD / "ppt" / "slideLayouts" / "slideLayout1.xml").write_text(layout, encoding="utf-8")
    (BUILD / "ppt" / "slideLayouts" / "_rels" / "slideLayout1.xml.rels").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideMaster" Target="../slideMasters/slideMaster1.xml"/></Relationships>''', encoding="utf-8")
    (BUILD / "ppt" / "theme" / "theme1.xml").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><a:theme xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main" name="FounderLink"><a:themeElements><a:clrScheme name="FounderLink"><a:dk1><a:srgbClr val="17202A"/></a:dk1><a:lt1><a:srgbClr val="FFFFFF"/></a:lt1><a:dk2><a:srgbClr val="0B1F33"/></a:dk2><a:lt2><a:srgbClr val="F6F7F9"/></a:lt2><a:accent1><a:srgbClr val="20B486"/></a:accent1><a:accent2><a:srgbClr val="6B7CFF"/></a:accent2><a:accent3><a:srgbClr val="F5A524"/></a:accent3><a:accent4><a:srgbClr val="D84B87"/></a:accent4><a:accent5><a:srgbClr val="3498DB"/></a:accent5><a:accent6><a:srgbClr val="607089"/></a:accent6><a:hlink><a:srgbClr val="3498DB"/></a:hlink><a:folHlink><a:srgbClr val="6B7CFF"/></a:folHlink></a:clrScheme><a:fontScheme name="FounderLink"><a:majorFont><a:latin typeface="Aptos"/></a:majorFont><a:minorFont><a:latin typeface="Aptos"/></a:minorFont></a:fontScheme><a:fmtScheme name="FounderLink"><a:fillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:fillStyleLst><a:lnStyleLst><a:ln w="9525"><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:ln></a:lnStyleLst><a:effectStyleLst><a:effectStyle><a:effectLst/></a:effectStyle></a:effectStyleLst><a:bgFillStyleLst><a:solidFill><a:schemeClr val="phClr"/></a:solidFill></a:bgFillStyleLst></a:fmtScheme></a:themeElements></a:theme>''', encoding="utf-8")

    for i, slide in enumerate(slides, 1):
        (BUILD / "ppt" / "slides" / f"slide{i}.xml").write_text(slide, encoding="utf-8")
        (BUILD / "ppt" / "slides" / "_rels" / f"slide{i}.xml.rels").write_text('''<?xml version="1.0" encoding="UTF-8" standalone="yes"?><Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slideLayout" Target="../slideLayouts/slideLayout1.xml"/></Relationships>''', encoding="utf-8")

    OUT.parent.mkdir(exist_ok=True)
    if OUT.exists():
        OUT.unlink()
    with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
        for path in BUILD.rglob("*"):
            if path.is_file():
                z.write(path, path.relative_to(BUILD).as_posix())


def font(size: int, bold: bool = False) -> ImageFont.FreeTypeFont:
    font_dir = Path("C:/Windows/Fonts")
    candidates = ["aptos-bold.ttf" if bold else "aptos.ttf", "arialbd.ttf" if bold else "arial.ttf"]
    for candidate in candidates:
        try:
            return ImageFont.truetype(str(font_dir / candidate), size)
        except OSError:
            continue
    return ImageFont.load_default()


def draw_wrapped(draw: ImageDraw.ImageDraw, xy: tuple[int, int], text: str, fnt, fill: str, max_width: int, line_gap: int = 6):
    x, y = xy
    for paragraph in text.split("\n"):
        words = paragraph.split(" ")
        line_words = []
        for word in words:
            trial = " ".join(line_words + [word])
            if draw.textbbox((0, 0), trial, font=fnt)[2] <= max_width or not line_words:
                line_words.append(word)
            else:
                draw.text((x, y), " ".join(line_words), font=fnt, fill=fill)
                y += fnt.size + line_gap
                line_words = [word]
        if line_words:
            draw.text((x, y), " ".join(line_words), font=fnt, fill=fill)
            y += fnt.size + line_gap
    return y


def preview() -> None:
    if PREVIEW_DIR.exists():
        shutil.rmtree(PREVIEW_DIR)
    PREVIEW_DIR.mkdir(parents=True)
    summaries = [
        ("FounderLink", "Microservices platform for founders, investors, teams, messaging, and notifications."),
        ("Problem Statement", "Startup collaboration is fragmented; FounderLink unifies core workflows through focused services."),
        ("Microservices Deployment View", "Angular -> API Gateway -> Spring Boot services, backed by Config, Eureka, PostgreSQL, Redis, and RabbitMQ."),
        ("ER Diagram", "Users, roles, profiles, startups, investments, teams, conversations, and messages."),
        ("Tech Stack", "Angular 20, Spring Boot, Spring Cloud, PostgreSQL, Redis, RabbitMQ, Docker, SonarQube, GitHub Actions."),
        ("API Endpoints", "Gateway-routed REST surface for auth, users, startups, investments, teams, messages, and notifications."),
        ("Microservice Descriptions", "Each service owns one business capability with clear runtime responsibility."),
        ("UI Components", "Auth page, dashboard, and service page compose the Angular experience."),
        ("UI Routing Endpoints", "/auth, /dashboard, and /services/:service with auth and guest guards."),
        ("Challenges Faced", "JWT consistency, service discovery, Docker orchestration, async events, and multi-project quality."),
        ("Learnings", "Microservices require ownership, contracts, config, discovery, auth boundaries, tests, and CI."),
        ("Future Enhancements", "Realtime messaging, richer discovery, notification inbox, file upload, Kubernetes, and observability."),
    ]
    for i, (title, body) in enumerate(summaries, 1):
        img = Image.new("RGB", (1600, 900), "#F6F7F9")
        d = ImageDraw.Draw(img)
        d.rectangle((0, 0, 1600, 900), fill="#F6F7F9")
        if i == 1:
            d.rectangle((0, 0, 620, 900), fill="#0B1F33")
            d.rectangle((620, 0, 640, 900), fill="#20B486")
            d.text((80, 170), title, font=font(76, True), fill="#FFFFFF")
            draw_wrapped(d, (84, 305), body, font(34), "#DCE8F3", 420)
            d.rectangle((790, 155, 1450, 690), outline="#D8DEE8", width=3, fill="#FFFFFF")
            for j, label in enumerate(["Angular UI", "API Gateway", "Services", "RabbitMQ", "PostgreSQL"]):
                x = 850 + (j % 2) * 280
                y = 235 + (j // 2) * 145
                d.rounded_rectangle((x, y, x + 220, y + 80), radius=16, fill="#E9F7F2" if j == 0 else "#EEF2FF", outline="#CBD5E1", width=2)
                d.text((x + 28, y + 26), label, font=font(23, True), fill="#17202A")
        else:
            d.text((70, 48), f"{i:02}", font=font(22, True), fill="#8A95A6")
            d.text((70, 95), title, font=font(48, True), fill="#17202A")
            d.line((70, 175, 1450, 175), fill="#20B486", width=7)
            draw_wrapped(d, (90, 250), body, font(34, True if i in [2, 11, 12] else False), "#27313F", 1240, 14)
            d.rounded_rectangle((1040, 610, 1450, 760), radius=18, fill="#0B1F33", outline="#0B1F33")
            d.text((1085, 655), "FounderLink", font=font(34, True), fill="#FFFFFF")
            d.text((1088, 705), "Project Presentation", font=font(22), fill="#9EE8CE")
        img.save(PREVIEW_DIR / f"slide_{i:02}.png")


def main() -> None:
    slides = build_slides()
    write_package(slides)
    preview()
    print(OUT)
    print(PREVIEW_DIR)


if __name__ == "__main__":
    main()
