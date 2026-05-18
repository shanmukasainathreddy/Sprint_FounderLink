const fs = require('fs');
const path = require('path');

const outputPath = path.resolve(__dirname, '..', 'docs', 'founderlink-oop-regex-exception-handling.pdf');

const doc = [
  { t: 'title', v: 'OOP Concepts, Regex, and Exception Handling in FounderLink' },
  { t: 'p', v: 'Generated: May 5, 2026' },
  { t: 'p', v: 'This PDF explains Object-Oriented Programming concepts used in the FounderLink Java microservices project, Java regular expressions with examples from the codebase, and Java 8 exception handling including the global exception handlers used in the project.' },

  { t: 'h1', v: '1. OOP Concepts Used in FounderLink' },
  { t: 'p', v: 'OOP means Object-Oriented Programming. In Java, it organizes software as classes and objects. FounderLink uses OOP heavily because every microservice has entities, DTOs, controllers, services, repositories, configuration classes, clients, producers, and exception handlers.' },

  { t: 'h2', v: '1.1 Class and Object' },
  { t: 'p', v: 'A class is a blueprint. An object is an instance created from that blueprint. In FounderLink, User, Role, Startup, Investment, Team, Conversation, Message, and NotificationEvent are classes. At runtime, Spring and Hibernate create/use objects from these classes.' },
  { t: 'b', v: 'Project example: auth-service entity User' },
  { t: 'code', v: '@Entity\n@Table(name = "users")\npublic class User {\n    private Long id;\n    private String email;\n    private String password;\n    private boolean enabled;\n}' },
  { t: 'p', v: 'When a user registers, AuthService creates a User object, fills email/password/OTP fields, and saves it through UserRepository.' },

  { t: 'h2', v: '1.2 Encapsulation' },
  { t: 'p', v: 'Encapsulation means keeping data and behavior together and controlling access to fields. Your entities and DTOs keep fields private. Lombok @Data generates getters and setters, so other classes access data through methods instead of directly touching fields.' },
  { t: 'b', v: 'Project examples' },
  { t: 'li', v: 'User has private fields such as email, password, otpCode, and otpExpiryAt.' },
  { t: 'li', v: 'Startup has private fields such as title, description, domain, status, fundingGoal, and userId.' },
  { t: 'li', v: 'DTOs like AuthRequest and UserRequest encapsulate request data sent from frontend to backend.' },
  { t: 'p', v: 'Why it matters: if fields are private, the class controls how data is read or changed. This helps validation, persistence, and maintainability.' },

  { t: 'h2', v: '1.3 Abstraction' },
  { t: 'p', v: 'Abstraction means hiding implementation details and exposing only what is needed. FounderLink uses abstraction through layers and interfaces.' },
  { t: 'b', v: 'Repository abstraction' },
  { t: 'code', v: 'public interface UserRepository extends JpaRepository<User, Long> {\n    Optional<User> findByEmail(String email);\n}' },
  { t: 'p', v: 'You do not write SQL for basic create/read/update/delete operations. JpaRepository hides the database implementation and gives methods like save(), findById(), and findAll().' },
  { t: 'b', v: 'Feign client abstraction' },
  { t: 'code', v: '@FeignClient(name = "user-service", path = "/users/internal")\npublic interface UserServiceClient {\n    @GetMapping("/{id}")\n    UserSummaryResponse getUserById(@PathVariable("id") Long id);\n}' },
  { t: 'p', v: 'Startup, investment, team, and messaging services call user-service through this interface. The code looks like a normal method call, while Feign handles the HTTP request internally.' },

  { t: 'h2', v: '1.4 Inheritance' },
  { t: 'p', v: 'Inheritance means one class/interface inherits behavior from another. FounderLink uses inheritance mainly through Spring and JPA framework types.' },
  { t: 'li', v: 'Repositories extend JpaRepository, so they inherit database operations.' },
  { t: 'li', v: 'JwtFilter extends OncePerRequestFilter, so it inherits servlet filter behavior and overrides doFilterInternal().' },
  { t: 'li', v: 'Spring Boot test classes inherit testing support through annotations and framework base behavior.' },
  { t: 'code', v: 'public class JwtFilter extends OncePerRequestFilter {\n    @Override\n    protected void doFilterInternal(...) throws ServletException, IOException {\n        // JWT validation logic\n    }\n}' },

  { t: 'h2', v: '1.5 Polymorphism' },
  { t: 'p', v: 'Polymorphism means one interface/type can behave differently depending on the implementation. FounderLink uses polymorphism through Spring dependency injection and framework interfaces.' },
  { t: 'li', v: 'AuthService depends on UserRepository, RoleRepository, NotificationProducer, and UserProfileClient. Spring injects actual runtime implementations.' },
  { t: 'li', v: 'Java interfaces like JpaRepository and Feign clients allow code to call methods without knowing the exact generated implementation class.' },
  { t: 'li', v: 'Exception handling is also polymorphic: @ExceptionHandler(Exception.class) can catch many exception subclasses if a more specific handler does not match first.' },

  { t: 'h2', v: '1.6 Association, Aggregation, and Composition' },
  { t: 'p', v: 'Association means classes are connected. Aggregation means one class uses another but both can exist separately. Composition means one object strongly owns another object.' },
  { t: 'li', v: 'AuthService is associated with UserRepository, RoleRepository, JwtUtil, NotificationProducer, and UserProfileClient.' },
  { t: 'li', v: 'MessagingService is associated with ConversationRepository, MessageRepository, NotificationProducer, and UserServiceClient.' },
  { t: 'li', v: 'Startup has a userId field. This is a loose association with the user profile stored in user-service.' },
  { t: 'p', v: 'Because FounderLink is microservice-based, it avoids strong object composition across services. Instead, services store ids and call other services when details are needed.' },

  { t: 'h2', v: '1.7 Constructor Injection' },
  { t: 'p', v: 'Constructor injection is an OOP and Spring dependency injection practice. Your services use Lombok @RequiredArgsConstructor, which generates constructors for final fields.' },
  { t: 'code', v: '@Service\n@RequiredArgsConstructor\npublic class InvestmentService {\n    private final InvestmentRepository repo;\n    private final StartupServiceClient startupServiceClient;\n    private final UserServiceClient userServiceClient;\n}' },
  { t: 'p', v: 'This makes dependencies explicit, improves testing, and avoids manually creating objects with new inside business classes.' },

  { t: 'h2', v: '1.8 Layered OOP Design in the Project' },
  { t: 'li', v: 'Controller classes handle HTTP endpoints, for example AuthController, StartupController, MessagingController.' },
  { t: 'li', v: 'Service classes contain business logic, for example AuthService, StartupService, InvestmentService.' },
  { t: 'li', v: 'Repository interfaces handle persistence, for example UserRepository and StartupRepository.' },
  { t: 'li', v: 'Entity classes represent database tables.' },
  { t: 'li', v: 'DTO classes represent request/response data.' },
  { t: 'li', v: 'Client interfaces represent service-to-service HTTP calls.' },

  { t: 'h1', v: '2. Regex in Java' },
  { t: 'p', v: 'Regex means Regular Expression. It is a pattern language used to match, validate, search, split, and replace text. Java supports regex through java.util.regex.Pattern, java.util.regex.Matcher, String.matches(), split(), replaceAll(), and validation annotations like @Pattern.' },

  { t: 'h2', v: '2.1 Why Regex Is Used' },
  { t: 'li', v: 'Validation: check if input has the expected format.' },
  { t: 'li', v: 'Searching: find text that matches a pattern.' },
  { t: 'li', v: 'Splitting: divide a string using a pattern.' },
  { t: 'li', v: 'Replacing: clean or normalize text.' },
  { t: 'li', v: 'Parsing: extract structured data from text.' },

  { t: 'h2', v: '2.2 Regex Types / Pattern Categories in Java' },
  { t: 'li', v: 'Literal pattern: matches exact text, for example "ADMIN".' },
  { t: 'li', v: 'Character class: [abc] matches a, b, or c. [A-Z] matches capital letters.' },
  { t: 'li', v: 'Negated class: [^0-9] matches any non-digit.' },
  { t: 'li', v: 'Predefined class: \\d means digit, \\w means word character, \\s means whitespace.' },
  { t: 'li', v: 'Quantifier: * means 0 or more, + means 1 or more, ? means optional, {6} means exactly 6.' },
  { t: 'li', v: 'Anchors: ^ means start of input, $ means end of input.' },
  { t: 'li', v: 'Alternation: A|B means A or B.' },
  { t: 'li', v: 'Grouping: (PENDING|APPROVED) groups options.' },
  { t: 'li', v: 'Case-insensitive flag: (?i) makes matching ignore uppercase/lowercase.' },

  { t: 'h2', v: '2.3 Regex Used in FounderLink' },
  { t: 'b', v: 'OTP validation in ResetPasswordRequest' },
  { t: 'code', v: '@Pattern(regexp = "\\\\d{6}", message = "OTP must be 6 digits")\nprivate String otp;' },
  { t: 'p', v: 'Meaning: \\d means digit and {6} means exactly six times. So the OTP must be six digits like 123456.' },
  { t: 'b', v: 'Investment status validation' },
  { t: 'code', v: '@Pattern(regexp = "^(?i)(PENDING|APPROVED|REJECTED|COMPLETED)$")\nprivate String status;' },
  { t: 'p', v: 'Meaning: ^ starts the match, (?i) ignores case, (...) groups allowed words, | means OR, and $ ends the match. Only PENDING, APPROVED, REJECTED, or COMPLETED are allowed.' },
  { t: 'b', v: 'Email validation' },
  { t: 'p', v: 'DTOs like AuthRequest and UserRequest use @Email. Internally, Bean Validation uses email validation logic based on email patterns. This protects endpoints from invalid email input.' },

  { t: 'h2', v: '2.4 String Pattern Operations Used in FounderLink' },
  { t: 'p', v: 'Your code also uses string operations that are common beside regex.' },
  { t: 'code', v: 'email.split("@")[0]\nroleName.trim().toUpperCase(Locale.ROOT)\nnormalizedRole.replace("-", "").replace("_", "").replace(" ", "")' },
  { t: 'p', v: 'split("@") is used to extract the email local part for default display names. replace() is used to normalize role names. If you used replaceAll("[^A-Z]", ""), that would be regex-based removal of all non-capital letters.' },

  { t: 'h2', v: '2.5 Java Regex APIs' },
  { t: 'code', v: 'Pattern pattern = Pattern.compile("\\\\d{6}");\nMatcher matcher = pattern.matcher("123456");\nboolean ok = matcher.matches();' },
  { t: 'li', v: 'Pattern.compile() compiles the regex.' },
  { t: 'li', v: 'matcher() applies the regex to text.' },
  { t: 'li', v: 'matches() checks the whole string.' },
  { t: 'li', v: 'find() searches for the next matching part.' },
  { t: 'li', v: 'replaceAll() replaces every matching part.' },

  { t: 'h1', v: '3. Exceptions in Java 8' },
  { t: 'p', v: 'An exception is an object that represents an abnormal condition during program execution. Java uses exceptions to separate normal logic from error-handling logic.' },

  { t: 'h2', v: '3.1 Checked Exceptions' },
  { t: 'p', v: 'Checked exceptions are checked at compile time. The method must handle them with try-catch or declare them with throws. Examples: IOException, SQLException, ClassNotFoundException.' },
  { t: 'p', v: 'In your JWT filters, doFilterInternal declares ServletException and IOException because servlet filtering can fail while reading/writing HTTP data.' },

  { t: 'h2', v: '3.2 Unchecked Exceptions' },
  { t: 'p', v: 'Unchecked exceptions extend RuntimeException. They are not forced by the compiler. FounderLink uses unchecked exceptions for business and validation failures.' },
  { t: 'li', v: 'IllegalArgumentException: invalid OTP, invalid password, wrong role, invalid authenticated user.' },
  { t: 'li', v: 'NoSuchElementException: user, startup, investment, or conversation not found.' },
  { t: 'li', v: 'IllegalStateException: authentication required or profile sync failed.' },
  { t: 'li', v: 'AccessDeniedException: user is not allowed to perform an action.' },

  { t: 'h2', v: '3.3 Error' },
  { t: 'p', v: 'Error represents serious JVM/system problems, such as OutOfMemoryError. Application code normally does not catch Error. Your global handlers target Exception, not Error, which is correct for web application behavior.' },

  { t: 'h2', v: '3.4 try-catch-finally in Java 8' },
  { t: 'code', v: 'try {\n    // risky operation\n} catch (RuntimeException ex) {\n    // recover or log\n} finally {\n    // cleanup, always runs\n}' },
  { t: 'p', v: 'FounderLink uses try-catch when notification sending should not break the main business action. For example, investment creation should still succeed even if founder email notification publishing fails.' },
  { t: 'code', v: 'try {\n    notificationProducer.sendNotification(...);\n} catch (RuntimeException ex) {\n    log.warn("Could not publish notification", ex);\n}' },

  { t: 'h2', v: '3.5 throw and throws' },
  { t: 'li', v: 'throw creates and raises an exception object, for example throw new IllegalArgumentException("Invalid OTP").' },
  { t: 'li', v: 'throws declares that a method may pass an exception to the caller, for example throws ServletException, IOException.' },

  { t: 'h1', v: '4. Global Exception Handling in Java/Spring' },
  { t: 'p', v: 'Global exception handling means one centralized class handles exceptions for all controllers in a service. In Spring Boot, this is usually done with @RestControllerAdvice and @ExceptionHandler.' },

  { t: 'h2', v: '4.1 Global Handler Used in FounderLink' },
  { t: 'p', v: 'Several services have GlobalExceptionHandler classes: auth-service, user-service, startup-service, messaging-service, and notification-service. These handlers convert Java exceptions into consistent JSON HTTP responses.' },
  { t: 'code', v: '@RestControllerAdvice\npublic class GlobalExceptionHandler {\n    @ExceptionHandler(IllegalArgumentException.class)\n    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException exception) {\n        return buildResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), exception);\n    }\n}' },

  { t: 'h2', v: '4.2 Exception to HTTP Status Mapping in Your Project' },
  { t: 'li', v: 'MethodArgumentNotValidException -> 400 BAD_REQUEST. Happens when @Valid DTO validation fails.' },
  { t: 'li', v: 'IllegalArgumentException -> 400 BAD_REQUEST. Happens for invalid business input like wrong OTP or wrong login role.' },
  { t: 'li', v: 'NoSuchElementException -> 404 NOT_FOUND. Happens when data is not found.' },
  { t: 'li', v: 'AccessDeniedException -> 403 FORBIDDEN in user-service. Happens when the user is authenticated but not allowed.' },
  { t: 'li', v: 'Exception -> 500 INTERNAL_SERVER_ERROR. Fallback for unexpected server errors.' },

  { t: 'h2', v: '4.3 Response Body Format' },
  { t: 'p', v: 'Your handlers build a Map response with timestamp, status, error, and message.' },
  { t: 'code', v: 'body.put("timestamp", Instant.now());\nbody.put("status", status.value());\nbody.put("error", status.getReasonPhrase());\nbody.put("message", message);' },
  { t: 'p', v: 'This gives the Angular frontend a predictable error format. The frontend can read message and show user-friendly messages.' },

  { t: 'h2', v: '4.4 Java 8 Feature Used in Exception Handling' },
  { t: 'p', v: 'Your validation handler uses Java 8 Stream and Optional style to extract the first validation error.' },
  { t: 'code', v: 'String message = exception.getBindingResult().getFieldErrors().stream()\n        .findFirst()\n        .map(error -> error.getDefaultMessage())\n        .orElse("Validation failed");' },
  { t: 'p', v: 'This is a Java 8 style pipeline: stream() creates a stream, findFirst() returns Optional, map() transforms the error into a message, and orElse() provides a default.' },

  { t: 'h2', v: '4.5 Example: Invalid OTP Flow' },
  { t: 'li', v: 'Frontend sends OTP to POST /auth/verify-otp.' },
  { t: 'li', v: 'AuthService checks saved OTP.' },
  { t: 'li', v: 'If OTP does not match, it throws IllegalArgumentException("Invalid OTP").' },
  { t: 'li', v: 'GlobalExceptionHandler catches IllegalArgumentException.' },
  { t: 'li', v: 'It returns HTTP 400 with JSON body containing message: Invalid OTP.' },
  { t: 'li', v: 'Angular reads the message and displays it to the user.' },

  { t: 'h2', v: '4.6 Example: Not Found Flow' },
  { t: 'li', v: 'Frontend or another service requests a user/startup/conversation by id.' },
  { t: 'li', v: 'Repository returns empty Optional.' },
  { t: 'li', v: 'Service calls orElseThrow() and throws NoSuchElementException.' },
  { t: 'li', v: 'GlobalExceptionHandler returns HTTP 404 NOT_FOUND.' },

  { t: 'h1', v: '5. How These Topics Connect in FounderLink' },
  { t: 'li', v: 'OOP gives the project structure: controllers, services, repositories, entities, DTOs, clients, producers, and handlers.' },
  { t: 'li', v: 'Regex and validation protect service methods from invalid input before business logic runs.' },
  { t: 'li', v: 'Exceptions represent failed validation, missing data, denied access, and unexpected failures.' },
  { t: 'li', v: 'Global exception handlers convert backend exceptions into clean HTTP responses for the frontend.' },
  { t: 'li', v: 'Together, these make the microservices easier to understand, test, debug, and maintain.' },

  { t: 'h1', v: '6. Important Project Files Referenced' },
  { t: 'li', v: 'auth-service/src/main/java/com/pro/auth_service/service/AuthService.java' },
  { t: 'li', v: 'auth-service/src/main/java/com/pro/auth_service/entity/User.java' },
  { t: 'li', v: 'auth-service/src/main/java/com/pro/auth_service/dto/ResetPasswordRequest.java' },
  { t: 'li', v: 'investment-service/src/main/java/com/pro/investment_service/dto/InvestmentStatusUpdateRequest.java' },
  { t: 'li', v: 'user-service/src/main/java/com/pro/user_service/service/UserService.java' },
  { t: 'li', v: 'startup-service/src/main/java/com/pro/startup_service/service/StartupService.java' },
  { t: 'li', v: 'messaging-service/src/main/java/com/pro/messaging_service/service/MessagingService.java' },
  { t: 'li', v: '*/exception/GlobalExceptionHandler.java in multiple services' },
];

function esc(s) {
  return String(s)
    .replace(/[^\x09\x0A\x0D\x20-\x7E]/g, '')
    .replace(/\\/g, '\\\\')
    .replace(/\(/g, '\\(')
    .replace(/\)/g, '\\)');
}

function wrap(text, max) {
  const out = [];
  for (const raw of String(text).split('\n')) {
    const words = raw.split(/\s+/).filter(Boolean);
    let line = '';
    for (const word of words) {
      if (!line) line = word;
      else if ((line + ' ' + word).length <= max) line += ' ' + word;
      else {
        out.push(line);
        line = word;
      }
    }
    out.push(line || '');
  }
  return out;
}

const pageW = 595.28;
const pageH = 841.89;
const margin = 46;
let pages = [];
let ops = [];
let y = pageH - margin;

function newPage() {
  if (ops.length) pages.push(ops);
  ops = [];
  y = pageH - margin;
}

function need(h) {
  if (y - h < margin) newPage();
}

function line(text, x, size, lead, font = 'F1') {
  ops.push(`BT /${font} ${size} Tf ${x.toFixed(2)} ${y.toFixed(2)} Td (${esc(text)}) Tj ET`);
  y -= lead;
}

function para(text, opts = {}) {
  const size = opts.size || 10;
  const lead = opts.lead || 14;
  const max = opts.max || 92;
  const x = opts.x || margin;
  const font = opts.font || 'F1';
  const before = opts.before || 0;
  const after = opts.after || 0;
  const prefix = opts.prefix || '';
  const lines = wrap(text, max);
  need(before + lines.length * lead + after);
  y -= before;
  lines.forEach((l, i) => line((i === 0 ? prefix : opts.indent || '') + l, x, size, lead, font));
  y -= after;
}

for (const item of doc) {
  if (item.t === 'title') para(item.v, { size: 23, lead: 28, max: 40, font: 'F2', after: 10 });
  else if (item.t === 'h1') para(item.v, { size: 15, lead: 20, max: 58, font: 'F2', before: 10, after: 3 });
  else if (item.t === 'h2') para(item.v, { size: 12, lead: 16, max: 70, font: 'F2', before: 5, after: 1 });
  else if (item.t === 'b') para(item.v, { size: 10, lead: 14, max: 90, font: 'F2', before: 2 });
  else if (item.t === 'li') para(item.v, { size: 10, lead: 14, max: 86, x: margin + 12, prefix: '- ', indent: '  ' });
  else if (item.t === 'code') para(item.v, { size: 8.5, lead: 11, max: 82, x: margin + 10, font: 'F3', before: 3, after: 5 });
  else para(item.v, { size: 10, lead: 14, max: 92, after: 3 });
}
if (ops.length) pages.push(ops);

function obj(id, body) {
  return `${id} 0 obj\n${body}\nendobj\n`;
}

const objects = [];
const catalogId = 1;
const pagesId = 2;
const f1 = 3;
const f2 = 4;
const f3 = 5;
let next = 6;
const pageIds = [];

objects.push(obj(catalogId, `<< /Type /Catalog /Pages ${pagesId} 0 R >>`));
objects.push(null);
objects.push(obj(f1, '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>'));
objects.push(obj(f2, '<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold >>'));
objects.push(obj(f3, '<< /Type /Font /Subtype /Type1 /BaseFont /Courier >>'));

for (const page of pages) {
  const pid = next++;
  const cid = next++;
  pageIds.push(pid);
  const stream = page.join('\n');
  objects.push(obj(pid, `<< /Type /Page /Parent ${pagesId} 0 R /MediaBox [0 0 ${pageW} ${pageH}] /Resources << /Font << /F1 ${f1} 0 R /F2 ${f2} 0 R /F3 ${f3} 0 R >> >> /Contents ${cid} 0 R >>`));
  objects.push(obj(cid, `<< /Length ${Buffer.byteLength(stream, 'latin1')} >>\nstream\n${stream}\nendstream`));
}
objects[1] = obj(pagesId, `<< /Type /Pages /Kids [${pageIds.map((id) => `${id} 0 R`).join(' ')}] /Count ${pageIds.length} >>`);

let pdf = '%PDF-1.4\n%\xE2\xE3\xCF\xD3\n';
const offsets = [0];
for (const o of objects) {
  offsets.push(Buffer.byteLength(pdf, 'latin1'));
  pdf += o;
}
const xref = Buffer.byteLength(pdf, 'latin1');
pdf += `xref\n0 ${objects.length + 1}\n0000000000 65535 f \n`;
for (let i = 1; i < offsets.length; i += 1) pdf += `${String(offsets[i]).padStart(10, '0')} 00000 n \n`;
pdf += `trailer\n<< /Size ${objects.length + 1} /Root ${catalogId} 0 R >>\nstartxref\n${xref}\n%%EOF\n`;

fs.mkdirSync(path.dirname(outputPath), { recursive: true });
fs.writeFileSync(outputPath, Buffer.from(pdf, 'latin1'));
console.log(`Wrote ${outputPath}`);
console.log(`Pages: ${pageIds.length}`);
