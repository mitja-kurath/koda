# Koda – Applikationssicherheit

**Modul 183 – Applikationssicherheit**  
**Team:** Alex, Daniel, Mitja  
**Schule:** GBS St. Gallen  

---

## 1. Projektbeschreibung

Koda ist eine Web-App zur Verwaltung und Veröffentlichung von API-Dokumentationen. Teams können OpenAPI-Spezifikationen hochladen, Markdown-Seiten schreiben und ihre Dokumentation öffentlich oder privat zugänglich machen.

**Tech Stack:**

| Schicht   | Technologie                                     |
|-----------|-------------------------------------------------|
| Frontend  | Angular 21, TypeScript, Tailwind CSS 4          |
| Backend   | Spring Boot 4, Java 25                          |
| Datenbank | PostgreSQL 17, Flyway (Schema-Migrationen)      |
| Auth      | JWT (HMAC-SHA256), BCrypt                       |
| Monorepo  | pnpm Workspaces                                 |

---

## 2. Gewählte OWASP-Kategorien

| # | Kategorie                               | Pflicht |
|---|-----------------------------------------|---------|
| A01:2025 | Broken Access Control              | ✅ empfohlen |
| A02:2025 | Cryptographic Failures             | ✅ gewählt |
| A03:2025 | Software and Data Integrity Failures | ✅ Pflicht |
| A07:2025 | Identification and Authentication Failures | ✅ gewählt |

---

## 3. A01:2025 – Broken Access Control

### Risiko

Ohne konsequente Zugriffskontrolle könnten Benutzer Projekte anderer Teams lesen, bearbeiten oder löschen. Auch die Rechtestufen innerhalb eines Teams (OWNER / ADMIN / MEMBER) müssten serverseitig erzwungen werden – eine rein clientseitige Prüfung wäre unzureichend.

### Massnahmen

**Teambasierte Projektisolierung**  
Jedes Projekt gehört einem Team. Der Zugriff wird in `ProjectService` bei jedem Request geprüft:

```java
// ProjectService.java
private void requireAccess(User user, Project project) {
    if (!teamMemberRepository.existsByTeamIdAndUserId(project.getTeamId(), user.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
}
```

**Rollenbasierte Rechtestufen**  
Drei Stufen: `OWNER`, `ADMIN`, `MEMBER`. Kritische Operationen (Projekt löschen, Mitglieder entfernen) sind auf den OWNER beschränkt:

```java
// ProjectService.java
private void requireOwner(User user, Project project) {
    teamMemberRepository.findByTeamIdAndUserId(project.getTeamId(), user.getId())
        .filter(m -> m.getRole() == TeamMember.Role.OWNER)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN,
            "Only the owner can perform this action"));
}
```

**Visibility-Kontrolle für öffentliche Projekte**  
Private Projekte erscheinen unter der öffentlichen URL (`/p/:slug`) als "Not Found" – nicht als "Forbidden" – um keine Information über die Existenz preiszugeben:

```java
// ProjectService.java
public ProjectResponse getPublicBySlug(String slug) {
    Project project = requireProject(slug);
    if (project.getVisibility() != Visibility.PUBLIC) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Project not found");
    }
    return ProjectResponse.from(project);
}
```

**Angular Route Guards**  
Geschützte Routen sind auf der Clientseite mit `authGuard` und `guestGuard` abgesichert. Da der Client nicht vertrauenswürdig ist, wird die eigentliche Autorisierung serverseitig erzwungen – die Guards dienen nur der UX.

**CORS**  
Nur die erlaubte Origin (`http://localhost:4200` in Dev, konfigurierbar per Umgebungsvariable) darf API-Requests stellen:

```java
// SecurityConfig.java
config.setAllowedOrigins(allowedOrigins); // ${koda.cors.allowed-origins}
config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
```

**Security Headers** (A05-Massnahme, auch relevant für A01)  
Alle Responses enthalten HTTP-Security-Headers, die Clickjacking und MIME-Sniffing verhindern:

```
X-Frame-Options: DENY
X-Content-Type-Options: nosniff
Content-Security-Policy: default-src 'self'; ...
Referrer-Policy: strict-origin-when-cross-origin
```

---

## 4. A02:2025 – Cryptographic Failures

### Risiko

Passwörter im Klartext gespeichert, schwache Hashing-Algorithmen (MD5/SHA-1), Tokens die direkt in der Datenbank liegen oder unsignierte JWTs würden Angreifern nach einem Datenbankzugriff vollständigen Account-Zugriff ermöglichen.

### Massnahmen

**BCrypt mit Stärke 12**  
Passwörter werden nie im Klartext gespeichert. BCrypt mit Cost-Factor 12 erzeugt automatisch einen Salt und ist bewusst langsam:

```java
// SecurityBeans.java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

**JWT mit HMAC-SHA256**  
Access Tokens werden mit einem 256-Bit-Secret signiert. Der Token enthält keine sensiblen Daten, nur die Benutzer-E-Mail als Subject:

```java
// JwtService.java
this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

return Jwts.builder()
    .subject(userDetails.getUsername())
    .issuedAt(new Date(now))
    .expiration(new Date(now + expiryMs))
    .signWith(signingKey)
    .compact();
```

**Refresh Tokens als SHA-256-Hash gespeichert**  
Der rohe Refresh Token wird nie in der Datenbank abgelegt. Nur der SHA-256-Hash wird persistiert. Falls die Datenbank kompromittiert wird, sind die Tokens wertlos:

```java
// RefreshTokenService.java
private String hash(String token) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] bytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
    return HexFormat.of().formatHex(bytes);
}

public void store(User user, String rawToken) {
    repository.save(RefreshToken.builder()
        .userId(user.getId())
        .tokenHash(hash(rawToken))   // nur Hash wird gespeichert
        .expiresAt(...)
        .build());
}
```

**Token-Rotation**  
Bei jedem Refresh wird der alte Token ungültig gemacht und ein neuer ausgestellt. Ein gestohlener Refresh Token kann nur einmal verwendet werden:

```java
// RefreshTokenService.java
public User rotate(String rawToken, Function<String, User> userLookup) {
    RefreshToken stored = repository.findByTokenHash(hash(rawToken))
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
    // ...
    repository.delete(stored);  // altes Token löschen
    return user;                // Caller speichert neues Token
}
```

**Konfiguration über Umgebungsvariablen**  
Secrets werden nie in den Source Code eingecheckt. `JWT_SECRET`, Datenbankpasswörter etc. kommen aus Umgebungsvariablen:

```yaml
# application.yaml
koda:
  jwt:
    secret: ${JWT_SECRET:dev-secret-change-in-production-must-be-256-bits-long}
```

**Kurze Token-Lebensdauer**  
Access Token: 15 Minuten (`900_000 ms`). Refresh Token: 7 Tage (`604_800_000 ms`). Kurze Lebensdauern begrenzen den Schaden bei einem Token-Diebstahl.

---

## 5. A03:2025 – Software and Data Integrity Failures

### Risiko

Nicht verifizierte Abhängigkeiten (Bibliotheken mit Schadfunktionen), keine Lock-Files, automatische Updates auf ungeprüfte Versionen oder das Einlesen von nicht validierten externen Daten (OpenAPI-Specs) können die Integrität der Anwendung gefährden.

### Massnahmen

**Gepinnte Abhängigkeiten (Backend)**  
Alle Bibliotheksversionen sind explizit in `build.gradle` fixiert. Die Spring Boot Dependency-Management-BOM stellt konsistente, geprüfte Versionen sicher:

```groovy
// build.gradle
id 'org.springframework.boot' version '4.0.6'          // fixierte Version
id 'io.spring.dependency-management' version '1.1.7'   // BOM-Management

implementation 'io.jsonwebtoken:jjwt-api:0.13.0'       // explizit gepinnt
implementation 'io.swagger.parser.v3:swagger-parser:2.1.22'
```

**Lock-File (Frontend)**  
Das pnpm-Monorepo verwendet `pnpm-lock.yaml`. Jeder `pnpm install` verwendet exakt die verifizierte Version mit dem geprüften Integrity-Hash:

```yaml
# pnpm-lock.yaml (Auszug)
angular/core@21.x.x:
  resolution: {integrity: sha512-...}
```

**Validierung hochgeladener OpenAPI-Spezifikationen**  
Benutzer können Dateien hochladen. Diese werden serverseitig durch den OpenAPI-Parser validiert, bevor sie gespeichert werden. Ungültige oder manipulierte Specs werden abgelehnt:

```java
// OpenApiValidationService.java
public OpenAPI parseAndValidate(String content) {
    SwaggerParseResult result = new OpenAPIParser().readContents(content, null, null);
    if (result.getOpenAPI() == null) {
        throw new IllegalArgumentException("Invalid OpenAPI spec: " + ...);
    }
    return result.getOpenAPI();
}
```

**Normalisierung vor der Speicherung**  
Die hochgeladene Spec (YAML oder JSON) wird nach der Validierung in normalisiertes JSON konvertiert. Das verhindert, dass inkonsistente oder gefährliche YAML-Konstrukte (z. B. YAML-Anchors, benutzerdefinierte Tags) gespeichert werden:

```java
// OpenApiValidationService.java
public String toJson(OpenAPI spec) {
    return objectMapper.writeValueAsString(spec);  // normalisiertes JSON
}
```

**Nur vertrauenswürdige Quellen (Maven Central)**  
Das Backend lädt Abhängigkeiten ausschliesslich von Maven Central. Keine privaten Repositories, keine direkten JAR-Downloads:

```groovy
// build.gradle
repositories {
    mavenCentral()
}
```

**XSS-Schutz bei Markdown**  
Markdown-Inhalte werden clientseitig gerendert. Angular's `DomSanitizer` bereinigt das HTML, bevor es in den DOM eingefügt wird – benutzerdefinierter Markup-Inhalt kann keinen Schadcode einschleusen:

```typescript
// markdown-view.ts
readonly html = computed(() => {
    const raw = marked.parse(this.content()) as string;
    return this.sanitizer.sanitize(SecurityContext.HTML, raw) ?? '';
});
```

---

## 6. A07:2025 – Identification and Authentication Failures

### Risiko

Schwache Passwort-Hashes, fehlende Brute-Force-Schutz, langlebige Sessions ohne Widerrufsmöglichkeit oder unsichere Token-Speicherung ermöglichen Account-Übernahmen.

### Massnahmen

**Brute-Force-Schutz (Rate Limiting)**  
Pro IP-Adresse + E-Mail-Kombination werden Fehlversuche gezählt. Nach 5 Fehlern ist das Konto für 15 Minuten gesperrt:

```java
// LoginAttemptService.java
public void recordFailure(String key) {
    attempts.compute(key, (k, existing) -> {
        int count = (existing == null ? 0 : existing.count()) + 1;
        Instant blockedUntil = count >= maxAttempts
            ? Instant.now().plusSeconds(lockoutMinutes * 60)
            : null;
        return new AttemptRecord(count, blockedUntil);
    });
}
```

Konfigurierbar in `application.yaml`:

```yaml
koda:
  rate-limit:
    max-attempts: 5
    lockout-minutes: 15
```

**Stateless Sessions mit JWT**  
Es werden keine serverseitigen Sessions verwaltet. Spring Security ist auf `STATELESS` konfiguriert. Tokens werden clientseitig im `localStorage` gespeichert und bei jedem Request als `Authorization: Bearer <token>` mitgeschickt:

```java
// SecurityConfig.java
.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
```

**Kurzer Access Token + Refresh Token Rotation**  
Access Token läuft nach 15 Minuten ab. Der Refresh Token wird bei jeder Verwendung rotiert (altes Token wird sofort ungültig). Logout widerruft alle Refresh Tokens des Benutzers:

```java
// AuthService.java
public void logout(User user, String ipAddress) {
    refreshTokenService.revokeAll(user);
    auditLogService.log(AuditLog.Event.LOGOUT, user.getId(), ipAddress, null);
}
```

**JWT-Validierung im Filter**  
Jeder Request läuft durch `JwtAuthenticationFilter`. Der Token wird signaturgeprüft und auf Ablauf geprüft, bevor der Request die Controller erreicht:

```java
// JwtAuthenticationFilter.java
if (jwtService.isTokenValid(jwt, userDetails)) {
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails, null, userDetails.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

**Audit Log**  
Alle sicherheitsrelevanten Ereignisse werden asynchron protokolliert (IP-Adresse, Zeitstempel, Event-Typ):

| Event              | Wann                                      |
|--------------------|-------------------------------------------|
| `LOGIN_SUCCESS`    | Erfolgreicher Login                       |
| `LOGIN_FAILURE`    | Falsches Passwort                         |
| `LOGIN_BLOCKED`    | Konto wegen zu vieler Versuche gesperrt   |
| `REGISTER_SUCCESS` | Neuer Account erstellt                    |
| `TOKEN_REFRESH`    | Refresh Token eingelöst                   |
| `LOGOUT`           | Benutzer hat sich ausgeloggt              |

```java
// AuditLogService.java
@Async
@Transactional
public void log(AuditLog.Event event, UUID userId, String ipAddress, String details) {
    repository.save(AuditLog.builder()
        .event(event)
        .userId(userId)
        .ipAddress(ipAddress)
        .details(details)
        .build());
}
```

**Input-Validierung**  
Alle Request-DTOs sind mit Bean Validation Constraints annotiert. Spring validiert vor der Verarbeitung:

```java
// RegisterRequest.java
public record RegisterRequest(
    @NotBlank @Size(min = 2, max = 100) String name,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 128) String password
) {}
```

---

## 7. Zusammenfassung

| Massnahme                              | Kategorie   | Implementierung                          |
|----------------------------------------|-------------|------------------------------------------|
| BCrypt Cost 12                         | A02, A07    | `SecurityBeans.java`                     |
| JWT HMAC-SHA256, 15 min Ablauf         | A02, A07    | `JwtService.java`                        |
| Refresh Token als SHA-256-Hash         | A02, A07    | `RefreshTokenService.java`               |
| Token-Rotation bei Refresh             | A07         | `RefreshTokenService.rotate()`           |
| Token-Widerruf bei Logout              | A07         | `RefreshTokenService.revokeAll()`        |
| Rate Limiting (5 Versuche / 15 min)    | A07         | `LoginAttemptService.java`               |
| Teambasierte Projektisolierung         | A01         | `ProjectService.requireAccess()`         |
| Rollenbasierte Berechtigungen (3 Stufen)| A01        | `ProjectService.requireOwner/Admin()`    |
| CORS mit Allowlist                     | A01         | `SecurityConfig.corsConfigurationSource()`|
| HTTP Security Headers                  | A01, A05    | `SecurityConfig` Header-Filter           |
| OpenAPI-Spec Validierung + Normalisierung | A03      | `OpenApiValidationService.java`          |
| XSS-Schutz für Markdown                | A03         | `MarkdownViewComponent` + DomSanitizer   |
| Gepinnte Abhängigkeiten + Lock-File    | A03         | `build.gradle`, `pnpm-lock.yaml`         |
| Secrets aus Umgebungsvariablen         | A02         | `application.yaml` + `.env`             |
| Audit Log (alle Auth-Events)           | A07         | `AuditLogService.java`                   |
| Input-Validierung mit Bean Validation  | A07         | DTOs mit `@Valid`-Constraints            |
| SQL Injection-Schutz                   | A01, A03    | JPA/Hibernate (parametrisierte Queries)  |
