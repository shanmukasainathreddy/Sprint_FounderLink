const fs = require('fs');
const path = require('path');

const outputPath = path.resolve(__dirname, '..', 'docs', 'FounderLink_Project_Flow_and_Code_Interview_Questions.pdf');

const today = 'May 11, 2026';

const doc = [
  { type: 'title', text: 'FounderLink Project Flow and Code Interview Questions' },
  { type: 'text', text: `Generated: ${today}` },
  { type: 'text', text: 'This PDF is prepared for interview revision. It explains the project flow and lists code-based questions an interviewer can ask from this FounderLink codebase.' },

  { type: 'h1', text: 'Project Flow Summary' },
  { type: 'text', text: 'FounderLink is a full-stack microservices platform that connects startup founders, investors, co-founders, and admins. The frontend is Angular. The backend is split into Spring Boot microservices. Docker Compose runs the supporting infrastructure.' },
  { type: 'flow', text: 'Browser -> Angular frontend -> API Gateway -> Spring Boot microservice -> Controller -> Service -> Repository / Feign / RabbitMQ -> PostgreSQL / Redis / Notification Service -> Response back to Angular' },
  { type: 'bullet', text: 'Angular frontend runs from sprint frontend/founderlink and manages session, forms, dashboards, and service pages through AppStore.' },
  { type: 'bullet', text: 'API Gateway is the single backend entry point on port 8080.' },
  { type: 'bullet', text: 'Config Server centralizes service configuration on port 8888.' },
  { type: 'bullet', text: 'Eureka Server provides service discovery on port 8761.' },
  { type: 'bullet', text: 'PostgreSQL stores auth, user, startup, investment, team, and messaging data.' },
  { type: 'bullet', text: 'Redis is used by user-service for user profile caching.' },
  { type: 'bullet', text: 'RabbitMQ carries notification events to notification-service.' },

  { type: 'h1', text: 'Main User Journey' },
  { type: 'bullet', text: 'A user opens the Angular app and chooses a role such as founder, investor, co-founder, or admin.' },
  { type: 'bullet', text: 'Registration goes to POST /auth/register through the gateway.' },
  { type: 'bullet', text: 'Auth service encodes the password, assigns a role, creates an OTP, syncs profile data to user-service, and publishes an OTP notification event.' },
  { type: 'bullet', text: 'User verifies OTP using POST /auth/verify-otp.' },
  { type: 'bullet', text: 'Login goes to POST /auth/login and returns a JWT.' },
  { type: 'bullet', text: 'Angular stores the JWT and sends it in the Authorization header for protected requests.' },
  { type: 'bullet', text: 'Founders create startup listings through startup-service.' },
  { type: 'bullet', text: 'Admins approve or reject startup listings.' },
  { type: 'bullet', text: 'Investors discover approved startups and create investment requests.' },
  { type: 'bullet', text: 'Founders review investment requests and update status.' },
  { type: 'bullet', text: 'Founders invite co-founders using team-service.' },
  { type: 'bullet', text: 'Users create conversations and send messages through messaging-service.' },
  { type: 'bullet', text: 'Notification-service consumes RabbitMQ events and sends emails.' },

  { type: 'h1', text: 'Service Responsibilities' },
  { type: 'h2', text: 'API Gateway' },
  { type: 'bullet', text: 'Routes frontend calls to /auth, /users, /startups, /investments, /teams, /messages, and /notifications.' },
  { type: 'bullet', text: 'Handles CORS and exposes aggregated Swagger links.' },
  { type: 'h2', text: 'Auth Service' },
  { type: 'bullet', text: 'Owns registration, OTP verification, login, roles, JWT creation, and password reset.' },
  { type: 'bullet', text: 'Uses PasswordEncoder, JJWT, PostgreSQL, Feign user profile sync, and RabbitMQ notification events.' },
  { type: 'h2', text: 'User Service' },
  { type: 'bullet', text: 'Owns profile data, directory data, internal user summaries, and profile sync from auth-service.' },
  { type: 'bullet', text: 'Uses Redis cache with @Cacheable and @CachePut.' },
  { type: 'h2', text: 'Startup Service' },
  { type: 'bullet', text: 'Owns startup creation, listing, search, update, admin review, and status notifications.' },
  { type: 'bullet', text: 'Gets authenticated founder id from SecurityContextHolder instead of trusting client-sent userId.' },
  { type: 'h2', text: 'Investment Service' },
  { type: 'bullet', text: 'Owns investment requests and status transitions such as PENDING, APPROVED, REJECTED, and COMPLETED.' },
  { type: 'bullet', text: 'Calls startup-service to find the founder and user-service to find founder email before sending a notification.' },
  { type: 'h2', text: 'Team Service' },
  { type: 'bullet', text: 'Owns co-founder invitations and accepted team memberships.' },
  { type: 'bullet', text: 'Checks that a user can accept only their own invite unless the requester is admin.' },
  { type: 'h2', text: 'Messaging Service' },
  { type: 'bullet', text: 'Owns conversations and messages.' },
  { type: 'bullet', text: 'Reuses existing conversations between two users and checks sender identity before saving messages.' },
  { type: 'h2', text: 'Notification Service' },
  { type: 'bullet', text: 'Consumes RabbitMQ notification.queue events and sends email.' },
  { type: 'bullet', text: 'Uses AmqpRejectAndDontRequeueException to avoid endless retries when email delivery fails.' },

  { type: 'h1', text: 'End-to-End Example Flows' },
  { type: 'h2', text: 'Registration Flow' },
  { type: 'flow', text: 'Angular register form -> POST /auth/register -> AuthController -> AuthService.register -> UserRepository + RoleRepository -> Feign UserProfileClient -> RabbitMQ notification -> OTP email' },
  { type: 'h2', text: 'Create Startup Flow' },
  { type: 'flow', text: 'Founder form -> POST /startups -> JwtFilter -> StartupController -> StartupService.create -> PostgreSQL save -> Feign user lookup -> RabbitMQ notification -> Angular state update' },
  { type: 'h2', text: 'Create Investment Flow' },
  { type: 'flow', text: 'Investor clicks Invest -> POST /investments -> InvestmentService.create -> save PENDING investment -> Feign startup lookup -> Feign user lookup -> RabbitMQ notification -> founder email' },
  { type: 'h2', text: 'Send Message Flow' },
  { type: 'flow', text: 'User sends message -> POST /messages -> MessagingService.sendMessage -> validate sender -> save message -> Feign user summaries -> RabbitMQ notification -> recipient email' },

  { type: 'h1', text: 'Architecture Questions' },
  { type: 'qa', q: 'Why did you choose microservices for FounderLink?', a: 'Each domain has a separate responsibility: auth, profiles, startups, investments, teams, messaging, and notifications. This improves separation of concerns and makes each service easier to test and scale independently.' },
  { type: 'qa', q: 'What is the role of API Gateway?', a: 'The gateway gives the frontend one backend URL and routes requests to the correct service. It also centralizes CORS and Swagger aggregation.' },
  { type: 'qa', q: 'Why are Config Server and Eureka used?', a: 'Config Server centralizes service properties. Eureka allows services to register and discover each other, which is useful when services move, restart, or scale.' },
  { type: 'qa', q: 'Why use RabbitMQ for notifications?', a: 'Email sending should not block user actions. Services publish NotificationEvent messages, and notification-service handles email delivery asynchronously.' },
  { type: 'qa', q: 'Why is Redis used only in user-service?', a: 'User profile reads are common across services. Redis reduces repeated database hits for profile lookup while keeping the source of truth in PostgreSQL.' },

  { type: 'h1', text: 'Spring Boot and Java Questions' },
  { type: 'qa', q: 'Explain the controller-service-repository pattern in this project.', a: 'Controllers expose REST endpoints, services contain business logic, and repositories handle persistence through Spring Data JPA.' },
  { type: 'qa', q: 'What does @RequiredArgsConstructor do here?', a: 'Lombok generates a constructor for final fields, so Spring can inject dependencies without manual constructor code.' },
  { type: 'qa', q: 'Where is business logic placed?', a: 'Business logic is mainly in service classes such as AuthService, StartupService, InvestmentService, TeamService, MessagingService, and UserService.' },
  { type: 'qa', q: 'Why is PasswordEncoder used in AuthService?', a: 'Passwords must not be stored as plain text. PasswordEncoder stores a secure hash and checks login using matches().' },
  { type: 'qa', q: 'How does AuthService normalize roles?', a: 'It trims input, uppercases it, removes ROLE_ prefix and separators, maps COFUNDER to COFOUNDER, and checks against role records.' },
  { type: 'qa', q: 'What happens if a user logs in without OTP verification?', a: 'AuthService generates a new OTP, saves it, sends a notification, and rejects login until verification is complete.' },
  { type: 'qa', q: 'Why does StartupService.create set startup id to null?', a: 'It prevents a detached-entity merge when the client accidentally sends an id during create.' },
  { type: 'qa', q: 'Why does StartupService use SecurityContextHolder?', a: 'It reads the authenticated user id from the JWT context, so the founder id cannot be forged in the request body.' },
  { type: 'qa', q: 'How is investment status handled?', a: 'InvestmentService creates new investments as PENDING and updateStatus converts provided status to uppercase before saving.' },
  { type: 'qa', q: 'Why does InvestmentService catch RuntimeException in notifyFounder?', a: 'Investment creation should still succeed even if startup lookup, user lookup, RabbitMQ, or email infrastructure is temporarily unavailable.' },

  { type: 'h1', text: 'Security Questions' },
  { type: 'qa', q: 'How is JWT used in this project?', a: 'Auth service generates JWTs after login. Other services use JWT filters to validate the Authorization header and set the authenticated user in the security context.' },
  { type: 'qa', q: 'Why is user id stored as JWT subject?', a: 'Services can identify the logged-in user without calling auth-service for every request.' },
  { type: 'qa', q: 'How does TeamService prevent accepting someone else’s invitation?', a: 'It compares the authenticated user id with request.userId and allows mismatch only for ROLE_ADMIN.' },
  { type: 'qa', q: 'How does MessagingService protect conversations?', a: 'It requires the authenticated user to be a participant when creating a conversation and allows users to view only their own conversation list.' },
  { type: 'qa', q: 'What is the risk if services trusted userId from frontend?', a: 'A malicious client could create startups, investments, messages, or team actions on behalf of another user.' },

  { type: 'h1', text: 'Database and JPA Questions' },
  { type: 'qa', q: 'Why use repositories instead of direct SQL in most places?', a: 'Spring Data JPA repositories reduce boilerplate and provide common CRUD operations with readable method names.' },
  { type: 'qa', q: 'Where is a custom database operation used?', a: 'UserRepository has an upsert-style profile sync used by auth-service registration and login recovery.' },
  { type: 'qa', q: 'Why validate email uniqueness in UserService?', a: 'It prevents two profiles from sharing the same email and allows the current user to keep their own email during update.' },
  { type: 'qa', q: 'What data belongs in auth-service and what belongs in user-service?', a: 'Auth owns credentials, roles, OTP, and JWT. User-service owns profile fields such as name, bio, skills, experience, links, and location.' },

  { type: 'h1', text: 'Feign and Service Communication Questions' },
  { type: 'qa', q: 'Where is Feign used?', a: 'Auth syncs profiles to user-service. Startup, investment, team, and messaging services fetch user summaries. Investment also calls startup-service.' },
  { type: 'qa', q: 'Why not duplicate founder email in Investment entity?', a: 'The source of truth for profile and email data is user-service. Investment stores business data and fetches profile summary when needed.' },
  { type: 'qa', q: 'What should happen if Feign call fails during notification?', a: 'The main operation should usually succeed, and notification failure should be logged or handled asynchronously.' },

  { type: 'h1', text: 'RabbitMQ Questions' },
  { type: 'qa', q: 'What is NotificationEvent?', a: 'It is a small DTO carrying email and message from producer services to notification-service.' },
  { type: 'qa', q: 'Which services publish notifications?', a: 'Auth, startup, investment, team, and messaging services publish notification events.' },
  { type: 'qa', q: 'What does @RabbitListener do?', a: 'It subscribes notification-service to the configured queue and invokes the consumer method when events arrive.' },
  { type: 'qa', q: 'Why reject without requeue on email failure?', a: 'If credentials or email data are invalid, requeueing forever can create repeated failures and block the queue.' },

  { type: 'h1', text: 'Angular Questions' },
  { type: 'qa', q: 'What is AppStore responsible for?', a: 'It centralizes frontend state, session token, users, startups, investments, invites, messages, notifications, and HTTP methods.' },
  { type: 'qa', q: 'How does Angular decide the backend URL?', a: 'resolveApiBase builds http://current-host:8080, so the frontend calls the gateway on the same host.' },
  { type: 'qa', q: 'Why store the JWT in frontend state/session?', a: 'The JWT must be added to Authorization headers for protected backend operations.' },
  { type: 'qa', q: 'What is the advantage of mapping backend DTOs to frontend models?', a: 'The UI can use clean names and types while the backend can keep its own entity and DTO shape.' },

  { type: 'h1', text: 'Testing and Quality Questions' },
  { type: 'qa', q: 'What kinds of tests exist in the project?', a: 'There are controller tests, service tests, repository tests, JWT filter tests, and application context tests across services.' },
  { type: 'qa', q: 'What is the purpose of SonarQube here?', a: 'SonarQube reports code quality, coverage, duplication, bugs, vulnerabilities, and maintainability issues.' },
  { type: 'qa', q: 'Why use JaCoCo?', a: 'JaCoCo generates Java test coverage reports that SonarQube can analyze.' },
  { type: 'qa', q: 'What would you test in AuthService?', a: 'Registration, duplicate email behavior, OTP expiry, invalid OTP, login before verification, wrong password, role mismatch, JWT generation, and password reset.' },
  { type: 'qa', q: 'What would you test in MessagingService?', a: 'Conversation reuse, sender validation, participant validation, message ordering, and notification publishing behavior.' },

  { type: 'h1', text: 'Common Interview Follow-ups' },
  { type: 'qa', q: 'Explain one complete flow from frontend click to database save.', a: 'Example: when a founder creates a startup, the Angular service page calls AppStore.saveStartup(). AppStore sends POST /startups to the API Gateway with the JWT token. The gateway routes the request to startup-service. JwtFilter validates the token and puts the authenticated user id into SecurityContextHolder. StartupController receives the request and calls StartupService.create(). The service sets id to null, takes founder userId from the authenticated context, saves the Startup entity through StartupRepository into PostgreSQL, optionally publishes a notification event, and returns the saved startup to Angular.' },
  { type: 'qa', q: 'Show where JWT validation happens and how the authenticated user id is used.', a: 'JWT validation happens in each protected service through its JwtFilter class. The filter reads the Authorization header, validates the token using JwtUtil, extracts the subject, and sets Authentication in SecurityContextHolder. Services such as StartupService, InvestmentService, TeamService, and MessagingService read SecurityContextHolder.getContext().getAuthentication().getName() to get the logged-in user id. This prevents clients from spoofing user ids in request bodies.' },
  { type: 'qa', q: 'Explain why notification is asynchronous.', a: 'Notification is asynchronous because email delivery can be slow or fail due to SMTP or network issues. In FounderLink, business services publish NotificationEvent messages to RabbitMQ and immediately continue their main operation. notification-service consumes the event later and sends email. This keeps registration, startup creation, investment creation, team invite, and messaging flows fast and resilient.' },
  { type: 'qa', q: 'Explain how services communicate with each other.', a: 'The frontend communicates only with API Gateway. The gateway routes HTTP requests to backend services. Backend services communicate synchronously using OpenFeign when they need immediate data, such as user-service profile summaries or startup-service startup details. They communicate asynchronously using RabbitMQ for notification events. Services also register with Eureka and read shared configuration from Config Server.' },
  { type: 'qa', q: 'Explain what you would improve if you had more time.', a: 'I would improve security, resilience, configuration, and testing. For security, I would add refresh tokens and stronger method-level role checks. For resilience, I would add circuit breakers around Feign calls and dead-letter queues for failed RabbitMQ messages. For maintainability, I would add a centralized error response format and use Docker secrets instead of plain environment passwords. For architecture, I would consider separate databases per service. For quality, I would add Testcontainers-based integration tests for PostgreSQL, RabbitMQ, Redis, and service interactions.' },
  { type: 'h2', text: 'Short Improvements List' },
  { type: 'bullet', text: 'Refresh tokens for safer long-running sessions.' },
  { type: 'bullet', text: 'Stronger role-based authorization using method-level checks like @PreAuthorize.' },
  { type: 'bullet', text: 'Centralized error response format for consistent frontend handling.' },
  { type: 'bullet', text: 'Circuit breakers and retries for Feign service-to-service calls.' },
  { type: 'bullet', text: 'Docker secrets or environment vaulting instead of plain passwords.' },
  { type: 'bullet', text: 'Separate databases per service to match true microservice ownership.' },
  { type: 'bullet', text: 'RabbitMQ dead-letter queues for failed notification messages.' },
  { type: 'bullet', text: 'Integration tests with Testcontainers for database, cache, queue, and service communication.' },
];

function clean(value) {
  return String(value)
    .replace(/[^\x09\x0A\x0D\x20-\x7E]/g, '')
    .replace(/\\/g, '\\\\')
    .replace(/\(/g, '\\(')
    .replace(/\)/g, '\\)');
}

function wrapText(text, maxChars) {
  const words = String(text).split(/\s+/).filter(Boolean);
  const lines = [];
  let current = '';
  for (const word of words) {
    if (!current) {
      current = word;
    } else if ((current + ' ' + word).length <= maxChars) {
      current += ' ' + word;
    } else {
      lines.push(current);
      current = word;
    }
  }
  if (current) lines.push(current);
  return lines;
}

const pageWidth = 595.28;
const pageHeight = 841.89;
const margin = 46;
const maxY = pageHeight - margin;
const minY = margin;
let pages = [];
let current = [];
let y = maxY;

function newPage() {
  if (current.length) pages.push(current);
  current = [];
  y = maxY;
}

function ensureSpace(height) {
  if (y - height < minY) newPage();
}

function addLine(text, x, fontSize, leading, font = 'F1') {
  current.push(`BT /${font} ${fontSize} Tf ${x.toFixed(2)} ${y.toFixed(2)} Td (${clean(text)}) Tj ET`);
  y -= leading;
}

function addWrapped(text, x, fontSize, leading, maxChars, options = {}) {
  const lines = wrapText(text, maxChars);
  ensureSpace(lines.length * leading + (options.before || 0) + (options.after || 0));
  if (options.before) y -= options.before;
  lines.forEach((line, index) => {
    const prefix = index === 0 && options.prefix ? options.prefix : options.hanging ? '  ' : '';
    addLine(prefix + line, x, fontSize, leading, options.font || 'F1');
  });
  if (options.after) y -= options.after;
}

for (const item of doc) {
  if (item.type === 'title') {
    ensureSpace(88);
    addWrapped(item.text, margin, 23, 29, 38, { font: 'F2', after: 8 });
  } else if (item.type === 'h1') {
    ensureSpace(44);
    y -= 10;
    addWrapped(item.text, margin, 16, 22, 54, { font: 'F2', after: 4 });
  } else if (item.type === 'h2') {
    ensureSpace(32);
    y -= 5;
    addWrapped(item.text, margin, 12, 17, 72, { font: 'F2', after: 2 });
  } else if (item.type === 'bullet') {
    addWrapped(item.text, margin + 14, 10, 14, 88, { prefix: '- ', hanging: true });
  } else if (item.type === 'flow') {
    addWrapped(item.text, margin + 8, 9, 13, 94, { before: 4, after: 4, font: 'F2' });
  } else if (item.type === 'qa') {
    addWrapped(`Q. ${item.q}`, margin, 10.5, 14, 90, { before: 3, font: 'F2' });
    addWrapped(`A. ${item.a}`, margin + 12, 10, 14, 88, { after: 4 });
  } else {
    addWrapped(item.text, margin, 10, 14, 92, { after: 3 });
  }
}
if (current.length) pages.push(current);

function makeObject(id, content) {
  return `${id} 0 obj\n${content}\nendobj\n`;
}

const objects = [];
const catalogId = 1;
const pagesId = 2;
const fontRegularId = 3;
const fontBoldId = 4;
const firstPageId = 5;
const pageIds = [];

objects.push(makeObject(catalogId, `<< /Type /Catalog /Pages ${pagesId} 0 R >>`));
objects.push(null);
objects.push(makeObject(fontRegularId, '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>'));
objects.push(makeObject(fontBoldId, '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>'));

let nextId = firstPageId;
for (const page of pages) {
  const pageId = nextId++;
  const contentId = nextId++;
  pageIds.push(pageId);
  const stream = page.join('\n');
  objects.push(makeObject(pageId, `<< /Type /Page /Parent ${pagesId} 0 R /MediaBox [0 0 ${pageWidth} ${pageHeight}] /Resources << /Font << /F1 ${fontRegularId} 0 R /F2 ${fontBoldId} 0 R >> >> /Contents ${contentId} 0 R >>`));
  objects.push(makeObject(contentId, `<< /Length ${Buffer.byteLength(stream, 'latin1')} >>\nstream\n${stream}\nendstream`));
}

objects[1] = makeObject(pagesId, `<< /Type /Pages /Kids [${pageIds.map((id) => `${id} 0 R`).join(' ')}] /Count ${pageIds.length} >>`);

let pdf = '%PDF-1.4\n%\xE2\xE3\xCF\xD3\n';
const offsets = [0];
for (const obj of objects) {
  offsets.push(Buffer.byteLength(pdf, 'latin1'));
  pdf += obj;
}
const xrefStart = Buffer.byteLength(pdf, 'latin1');
pdf += `xref\n0 ${objects.length + 1}\n`;
pdf += '0000000000 65535 f \n';
for (let i = 1; i < offsets.length; i += 1) {
  pdf += `${String(offsets[i]).padStart(10, '0')} 00000 n \n`;
}
pdf += `trailer\n<< /Size ${objects.length + 1} /Root ${catalogId} 0 R >>\nstartxref\n${xrefStart}\n%%EOF\n`;

fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, Buffer.from(pdf, 'latin1'));
console.log(`Wrote ${outputPath}`);
console.log(`Pages: ${pageIds.length}`);
