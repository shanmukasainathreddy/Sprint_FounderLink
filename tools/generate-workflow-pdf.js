const fs = require('fs');
const path = require('path');

const outputPath = path.resolve(__dirname, '..', 'docs', 'founderlink-microservices-workflows.pdf');

const doc = [
  { type: 'title', text: 'FounderLink Microservices End-to-End Workflows' },
  { type: 'text', text: 'Generated: May 4, 2026' },
  { type: 'text', text: 'This document explains how every major user action moves from the Angular frontend, through the API Gateway, into each Spring Boot microservice, and finally into storage, service-to-service calls, RabbitMQ notifications, or email delivery.' },

  { type: 'h1', text: 'System Overview' },
  { type: 'text', text: 'FounderLink is a full-stack microservices platform for founders, investors, co-founders, and admins. The Angular frontend uses a central AppStore to manage forms, session state, service-page actions, and HTTP calls. The frontend talks to the backend through one gateway URL: http://localhost:8080.' },
  { type: 'flow', text: 'Browser user -> Angular frontend -> API Gateway -> Microservice controller -> Service layer -> Repository / Feign / RabbitMQ -> Response -> Angular state update' },
  { type: 'h2', text: 'Shared Runtime Pattern' },
  { type: 'bullet', text: 'User performs an action in the Angular UI.' },
  { type: 'bullet', text: 'Angular calls a method in AppStore, such as login(), saveStartup(), createInvestment(), sendInvite(), or sendMessage().' },
  { type: 'bullet', text: 'HttpClient sends the request to http://localhost:8080.' },
  { type: 'bullet', text: 'API Gateway routes the request by path.' },
  { type: 'bullet', text: 'Protected services validate the JWT from the Authorization header.' },
  { type: 'bullet', text: 'The controller calls the service layer.' },
  { type: 'bullet', text: 'The service layer saves data in PostgreSQL, reads Redis cache where used, calls other services through Feign, or publishes notification events to RabbitMQ.' },
  { type: 'bullet', text: 'The frontend receives the response and updates local Angular signals/state.' },
  { type: 'h2', text: 'Gateway Routes' },
  { type: 'bullet', text: '/auth/** -> auth-service -> port 8081' },
  { type: 'bullet', text: '/users/** -> user-service -> port 8082' },
  { type: 'bullet', text: '/startups/** -> startup-service -> port 8083' },
  { type: 'bullet', text: '/investments/** -> investment-service -> port 8084' },
  { type: 'bullet', text: '/teams/** -> team-service -> port 8085' },
  { type: 'bullet', text: '/messages/** -> messaging-service -> port 8086' },
  { type: 'bullet', text: '/notifications/** and /api/notifications/** -> notification-service -> port 8087' },

  { type: 'h1', text: 'API Gateway Workflow' },
  { type: 'text', text: 'The gateway is the single backend entry point for the frontend. It keeps the Angular app independent from individual service URLs and centralizes CORS and Swagger aggregation.' },
  { type: 'bullet', text: 'Angular builds a request URL using API_BASE, usually http://localhost:8080.' },
  { type: 'bullet', text: 'The browser sends the request to the gateway.' },
  { type: 'bullet', text: 'Gateway matches the request path, for example /startups.' },
  { type: 'bullet', text: 'Gateway forwards the request to the configured service URL, for example startup-service:8083.' },
  { type: 'bullet', text: 'The target service returns JSON or text to the gateway.' },
  { type: 'bullet', text: 'Gateway returns the response to Angular.' },

  { type: 'h1', text: 'Auth Service Workflow' },
  { type: 'text', text: 'Auth service owns registration, OTP verification, login, role assignment, JWT generation, and password reset. It also syncs profile data into user-service.' },
  { type: 'h2', text: 'Frontend Actions and Endpoints' },
  { type: 'bullet', text: 'Register -> POST /auth/register -> create pending user, assign role, sync profile, send OTP.' },
  { type: 'bullet', text: 'Verify OTP -> POST /auth/verify-otp -> enable account after OTP validation.' },
  { type: 'bullet', text: 'Resend OTP -> POST /auth/resend-otp -> generate and send a new OTP.' },
  { type: 'bullet', text: 'Login -> POST /auth/login -> validate credentials and return JWT.' },
  { type: 'bullet', text: 'Forgot password -> POST /auth/forgot-password -> generate password reset OTP.' },
  { type: 'bullet', text: 'Verify reset OTP -> POST /auth/forgot-password/verify -> validate reset OTP before password change.' },
  { type: 'bullet', text: 'Reset password -> POST /auth/reset-password -> update encoded password.' },
  { type: 'h2', text: 'Registration Flow' },
  { type: 'bullet', text: 'User fills role, personal info, and profile details on the Angular auth page.' },
  { type: 'bullet', text: 'AppStore.register() posts email, password, role, name, and bio to /auth/register.' },
  { type: 'bullet', text: 'AuthController passes the request to AuthService.register().' },
  { type: 'bullet', text: 'Auth service creates or refreshes a pending user record in PostgreSQL.' },
  { type: 'bullet', text: 'Password is encoded with Spring Security.' },
  { type: 'bullet', text: 'Requested role is normalized and linked through the user-role table.' },
  { type: 'bullet', text: 'Auth service calls user-service through Feign at /users/internal/sync.' },
  { type: 'bullet', text: 'Auth service publishes an OTP notification event to RabbitMQ.' },
  { type: 'bullet', text: 'Frontend stores pending verification email and shows the OTP step.' },
  { type: 'h2', text: 'Login Flow' },
  { type: 'bullet', text: 'User submits email, password, and selected role.' },
  { type: 'bullet', text: 'Auth service finds the user by email and checks the account is enabled.' },
  { type: 'bullet', text: 'If not verified, a new OTP is generated and sent, and login fails with a helpful message.' },
  { type: 'bullet', text: 'Password is checked with PasswordEncoder.matches().' },
  { type: 'bullet', text: 'Selected role must match assigned role.' },
  { type: 'bullet', text: 'Auth service ensures the profile exists in user-service.' },
  { type: 'bullet', text: 'JWT is generated with user id as subject and roles as claims.' },
  { type: 'bullet', text: 'Angular stores sessionUserId and sessionToken, then refreshes backend data.' },

  { type: 'h1', text: 'User Service Workflow' },
  { type: 'text', text: 'User service owns public profile information and user directory summaries. Authentication data stays in auth-service; profile data stays here.' },
  { type: 'h2', text: 'Endpoints' },
  { type: 'bullet', text: 'POST /users -> create profile directly.' },
  { type: 'bullet', text: 'GET /users -> return full profile list.' },
  { type: 'bullet', text: 'GET /users/directory -> return profile summaries for frontend lists.' },
  { type: 'bullet', text: 'GET /users/{id} -> return one profile.' },
  { type: 'bullet', text: 'GET /users/internal/{id} -> service-to-service user lookup.' },
  { type: 'bullet', text: 'POST /users/internal/sync -> auth service syncs profile after registration/login recovery.' },
  { type: 'bullet', text: 'PUT /users/{id} -> save profile edits from frontend.' },
  { type: 'h2', text: 'Save Profile Flow' },
  { type: 'bullet', text: 'User edits profile fields on the Profile Service page.' },
  { type: 'bullet', text: 'AppStore.saveProfile() sends PUT /users/{currentUserId} with JWT.' },
  { type: 'bullet', text: 'User service validates the JWT and request body.' },
  { type: 'bullet', text: 'UserService.update() checks email uniqueness.' },
  { type: 'bullet', text: 'Profile fields are copied into the existing entity.' },
  { type: 'bullet', text: 'PostgreSQL is updated.' },
  { type: 'bullet', text: 'Redis cache is refreshed through @CachePut.' },
  { type: 'bullet', text: 'Angular merges the returned profile into local state.' },
  { type: 'h2', text: 'Internal Lookup Flow' },
  { type: 'text', text: 'Startup, investment, team, and messaging services call user-service internally to get names and emails. This avoids copying profile data into every service.' },

  { type: 'h1', text: 'Startup Service Workflow' },
  { type: 'text', text: 'Startup service owns startup listings, founder ownership, admin review status, and startup lifecycle notifications.' },
  { type: 'h2', text: 'Endpoints' },
  { type: 'bullet', text: 'GET /startups -> return listings for dashboards, discovery, and admin.' },
  { type: 'bullet', text: 'POST /startups -> founder creates a listing.' },
  { type: 'bullet', text: 'GET /startups/{id} -> used by other services, especially investment-service.' },
  { type: 'bullet', text: 'PUT /startups/{id} -> founder edits listing or admin updates status.' },
  { type: 'bullet', text: 'DELETE /startups/{id} -> admin deletes a listing.' },
  { type: 'h2', text: 'Create Startup Flow' },
  { type: 'bullet', text: 'Founder opens Startup Service page and fills startup name, industry, stage, funding goal, problem, solution, and pitch.' },
  { type: 'bullet', text: 'AppStore.saveStartup() sends POST /startups.' },
  { type: 'bullet', text: 'Gateway forwards request to startup-service.' },
  { type: 'bullet', text: 'JWT filter identifies the logged-in founder.' },
  { type: 'bullet', text: 'StartupService.create() clears any sent id and sets userId from the authenticated token.' },
  { type: 'bullet', text: 'Startup is saved in PostgreSQL with initial status.' },
  { type: 'bullet', text: 'Startup service gets founder email through user-service Feign client.' },
  { type: 'bullet', text: 'Startup creation notification is published to RabbitMQ.' },
  { type: 'bullet', text: 'Angular receives saved startup and adds it to local startup state.' },
  { type: 'h2', text: 'Admin Review Flow' },
  { type: 'bullet', text: 'Admin opens Admin Service page and chooses approve or reject.' },
  { type: 'bullet', text: 'Angular calls reviewStartup(), which sends PUT /startups/{id}.' },
  { type: 'bullet', text: 'Startup service updates the status.' },
  { type: 'bullet', text: 'If status changes to accepted or rejected, startup service notifies the founder through RabbitMQ.' },
  { type: 'bullet', text: 'Approved startups become visible on the Discovery page.' },

  { type: 'h1', text: 'Investment Service Workflow' },
  { type: 'text', text: 'Investment service owns investment requests and investment status transitions. It uses startup-service and user-service to notify founders about new investment interest.' },
  { type: 'h2', text: 'Endpoints' },
  { type: 'bullet', text: 'POST /investments -> investor creates pending investment request.' },
  { type: 'bullet', text: 'GET /investments -> frontend refresh path for current containers.' },
  { type: 'bullet', text: 'GET /investments/startup/{id} -> founder reviews requests for their startup.' },
  { type: 'bullet', text: 'GET /investments/investor/{id} -> investor portfolio view.' },
  { type: 'bullet', text: 'PUT /investments/{id}/status -> founder approves, rejects, or completes investment.' },
  { type: 'h2', text: 'Create Investment Flow' },
  { type: 'bullet', text: 'Investor selects an approved startup and enters an amount.' },
  { type: 'bullet', text: 'Angular sends POST /investments with startup id and amount.' },
  { type: 'bullet', text: 'Investment service validates JWT and sets investorId from the authenticated user.' },
  { type: 'bullet', text: 'Status is set to PENDING.' },
  { type: 'bullet', text: 'Investment is saved in PostgreSQL.' },
  { type: 'bullet', text: 'Investment service calls startup-service to load the startup and founder id.' },
  { type: 'bullet', text: 'Investment service calls user-service to load founder email.' },
  { type: 'bullet', text: 'Notification event is published to RabbitMQ for the founder.' },
  { type: 'bullet', text: 'Angular adds the investment to the investor portfolio and updates progress calculations.' },
  { type: 'h2', text: 'Founder Status Update Flow' },
  { type: 'bullet', text: 'Founder opens Investment Service page and sees requests for their startups.' },
  { type: 'bullet', text: 'Founder can approve or reject a pending request.' },
  { type: 'bullet', text: 'Approved requests can later be marked completed.' },
  { type: 'bullet', text: 'Angular sends PUT /investments/{id}/status.' },
  { type: 'bullet', text: 'Investment service updates status in PostgreSQL.' },
  { type: 'bullet', text: 'Frontend updates the cards and status hints.' },

  { type: 'h1', text: 'Team Service Workflow' },
  { type: 'text', text: 'Team service owns invitations and accepted startup memberships.' },
  { type: 'h2', text: 'Endpoints' },
  { type: 'bullet', text: 'POST /teams/invite -> founder/admin invites a cofounder.' },
  { type: 'bullet', text: 'POST /teams/join -> cofounder accepts their invitation.' },
  { type: 'bullet', text: 'GET /teams/startup/{id} -> show members/invites for a startup.' },
  { type: 'bullet', text: 'GET /teams/my -> show active teams for logged-in user.' },
  { type: 'h2', text: 'Invite Cofounder Flow' },
  { type: 'bullet', text: 'Founder selects startup, invitee, and role.' },
  { type: 'bullet', text: 'AppStore.sendInvite() sends POST /teams/invite.' },
  { type: 'bullet', text: 'Team service creates or updates a team row for that startup and user.' },
  { type: 'bullet', text: 'Status is set to PENDING.' },
  { type: 'bullet', text: 'Team service calls user-service to find the invited user email.' },
  { type: 'bullet', text: 'Notification event is published to RabbitMQ.' },
  { type: 'bullet', text: 'Angular adds the invite to local state.' },
  { type: 'h2', text: 'Accept Invitation Flow' },
  { type: 'bullet', text: 'Cofounder opens Team Service page and sees incoming invites.' },
  { type: 'bullet', text: 'Angular sends POST /teams/join.' },
  { type: 'bullet', text: 'Team service checks the JWT user matches the invited user unless the requester is admin.' },
  { type: 'bullet', text: 'Existing pending invitation is loaded.' },
  { type: 'bullet', text: 'Status changes from PENDING to ACTIVE.' },
  { type: 'bullet', text: 'Notification event confirms the join.' },
  { type: 'bullet', text: 'Frontend maps ACTIVE to accepted invite state.' },

  { type: 'h1', text: 'Messaging Service Workflow' },
  { type: 'text', text: 'Messaging service owns conversations and messages. It also ensures users can access only their own conversations.' },
  { type: 'h2', text: 'Endpoints' },
  { type: 'bullet', text: 'POST /messages/conversation -> create or reuse a conversation between two users.' },
  { type: 'bullet', text: 'POST /messages -> store a message and notify recipient.' },
  { type: 'bullet', text: 'GET /messages/conversation/{id} -> read conversation thread.' },
  { type: 'bullet', text: 'GET /messages/user/{id}/conversations -> list conversations for logged-in user.' },
  { type: 'h2', text: 'Open Conversation Flow' },
  { type: 'bullet', text: 'User selects a message contact from the service page.' },
  { type: 'bullet', text: 'Angular calls AppStore.openConversation(partnerId).' },
  { type: 'bullet', text: 'Frontend sends POST /messages/conversation with current user and partner ids.' },
  { type: 'bullet', text: 'Messaging service confirms the authenticated user is one participant.' },
  { type: 'bullet', text: 'Existing conversation is reused if found in either participant order.' },
  { type: 'bullet', text: 'If none exists, a new conversation is saved in PostgreSQL.' },
  { type: 'bullet', text: 'Angular sets the selected conversation id.' },
  { type: 'h2', text: 'Send Message Flow' },
  { type: 'bullet', text: 'User types a message and clicks send.' },
  { type: 'bullet', text: 'Angular sends POST /messages with conversation id, sender id, and content.' },
  { type: 'bullet', text: 'Messaging service checks sender id matches JWT subject.' },
  { type: 'bullet', text: 'Conversation is loaded and recipient is resolved.' },
  { type: 'bullet', text: 'Message is saved with current timestamp.' },
  { type: 'bullet', text: 'Service calls user-service for sender and recipient summary data.' },
  { type: 'bullet', text: 'RabbitMQ notification is published for recipient email.' },
  { type: 'bullet', text: 'Frontend appends the message locally and also refreshes messages periodically every 5 seconds.' },

  { type: 'h1', text: 'Notification Service Workflow' },
  { type: 'text', text: 'Notification service decouples email delivery from user-facing operations. Producers only publish events; this service consumes those events and sends email.' },
  { type: 'h2', text: 'Endpoint and Event' },
  { type: 'bullet', text: 'GET /api/notifications/status -> frontend checks notification-service availability.' },
  { type: 'bullet', text: 'notification.queue -> RabbitMQ listener receives notification events.' },
  { type: 'h2', text: 'Email Event Processing' },
  { type: 'bullet', text: 'Auth, startup, investment, team, or messaging service creates a NotificationEvent.' },
  { type: 'bullet', text: 'Producer sends the event to notification.exchange with routing key notification.routing-key.' },
  { type: 'bullet', text: 'RabbitMQ places the message on notification.queue.' },
  { type: 'bullet', text: 'NotificationConsumer receives the event with @RabbitListener.' },
  { type: 'bullet', text: 'NotificationService.sendEmail() validates SMTP username and password.' },
  { type: 'bullet', text: 'Spring Mail sends an email with subject FounderLink Notification.' },
  { type: 'bullet', text: 'If delivery fails, the consumer rejects without requeueing.' },

  { type: 'h1', text: 'Config Server Workflow' },
  { type: 'text', text: 'Config server provides centralized service configuration. It is not called by the frontend. Backend services use it during startup to load configuration such as ports, service URLs, database settings, RabbitMQ settings, and discovery settings.' },
  { type: 'bullet', text: 'Config server starts on port 8888.' },
  { type: 'bullet', text: 'Each service starts and reads its configuration from config server or local fallback properties.' },
  { type: 'bullet', text: 'Service-specific files live under config-server/src/main/resources/config.' },
  { type: 'bullet', text: 'Centralized config reduces duplication across microservices.' },

  { type: 'h1', text: 'Eureka Server Workflow' },
  { type: 'text', text: 'Eureka server provides service discovery. It is also not called by the frontend.' },
  { type: 'bullet', text: 'Eureka server starts on port 8761.' },
  { type: 'bullet', text: 'Microservices register themselves with Eureka during startup.' },
  { type: 'bullet', text: 'Gateway and Feign clients can discover services through the registry.' },
  { type: 'bullet', text: 'This helps when services restart, move, or scale.' },

  { type: 'h1', text: 'Complete Example: Investor Creates Investment' },
  { type: 'flow', text: 'Investor clicks Invest -> Angular AppStore.createInvestment() -> POST /investments -> API Gateway -> investment-service -> JWT validation -> PostgreSQL save -> Feign startup-service -> Feign user-service -> RabbitMQ -> notification-service -> founder email -> Angular state update' },
  { type: 'bullet', text: 'Investor finds an approved startup on Discovery page.' },
  { type: 'bullet', text: 'Investor clicks Invest and enters amount.' },
  { type: 'bullet', text: 'Angular sends the request through the gateway with JWT.' },
  { type: 'bullet', text: 'Investment service saves a pending request.' },
  { type: 'bullet', text: 'Investment service finds the startup founder using startup-service.' },
  { type: 'bullet', text: 'Investment service finds founder email using user-service.' },
  { type: 'bullet', text: 'Notification event is published to RabbitMQ.' },
  { type: 'bullet', text: 'Notification service sends email to founder.' },
  { type: 'bullet', text: 'Frontend updates portfolio and dashboard progress.' },

  { type: 'h1', text: 'Summary' },
  { type: 'text', text: 'The application follows a consistent microservice workflow: Angular action, gateway route, JWT validation, controller, service layer, repository persistence, optional Feign lookup, optional RabbitMQ event, and frontend state refresh. Each service owns one business area, which keeps authentication, profiles, startups, investments, teams, messages, and email delivery separated but connected.' },
];

function escapePdfText(value) {
  return value
    .replace(/[^\x09\x0A\x0D\x20-\x7E]/g, '')
    .replace(/\\/g, '\\\\')
    .replace(/\(/g, '\\(')
    .replace(/\)/g, '\\)');
}

function wrapText(text, maxChars) {
  const words = text.split(/\s+/).filter(Boolean);
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
const margin = 48;
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
  current.push(`BT /${font} ${fontSize} Tf ${x.toFixed(2)} ${y.toFixed(2)} Td (${escapePdfText(text)}) Tj ET`);
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
    ensureSpace(80);
    addWrapped(item.text, margin, 24, 30, 36, { font: 'F2', after: 8 });
  } else if (item.type === 'h1') {
    ensureSpace(42);
    y -= 10;
    addWrapped(item.text, margin, 16, 22, 54, { font: 'F2', after: 4 });
  } else if (item.type === 'h2') {
    ensureSpace(30);
    y -= 5;
    addWrapped(item.text, margin, 12, 17, 72, { font: 'F2', after: 2 });
  } else if (item.type === 'bullet') {
    addWrapped(item.text, margin + 14, 10, 14, 88, { prefix: '- ', hanging: true });
  } else if (item.type === 'flow') {
    addWrapped(item.text, margin + 8, 9, 13, 93, { before: 4, after: 4 });
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
