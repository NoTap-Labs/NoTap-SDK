# OWASP WSTG Scanner — Reporte de Pentesting

- **Objetivo:** `http://app.notap.io`
- **Fecha:** 2026-06-08 19:52:32
- **Herramienta:** WSTG Scanner v1.4.2

## Resumen ejecutivo

| Campo | Valor |
| --- | --- |
| Status HTTP | 404 |
| Servidor | railway-hikari |
| Tecnologías | Country (UNITED STATES][US), IP (69.46.46.120), RedirectLocation (https://app.notap.io/), UncommonHeaders (x-railway-67), HTTPServer (railway-hikari), UncommonHeaders (x-railway-edge,x-railway-fallback,x-railway-request-id,x-hikari-trace) |
| Hallazgos (FINDINGS) | 68 |
| Puertos abiertos (nmap) | 2 |
| Resultados NSE dirigidos | 35 |
| Vulnerabilidades Nuclei | 3 |
| URLs spider | 1 |
| Subdominios (vhosts) | 0 |
| Directorios encontrados | 0 |
| Endpoints API | 1330 |
| Usuarios | 0 |
| Emails | 0 |
| Credenciales válidas | 0 |
| WordPress vulnerabilidades | 0 |
| Usuarios AD (LDAP) | 0 |
| AS-REP roastable | 0 |
| Kerberoastable SPNs | 0 |
| Credenciales AD (NXC) | 0 |
| Hallazgos en código fuente | 0 |

## Cabeceras de seguridad

| Header | Estado | Valor |
| --- | --- | --- |
| Strict-Transport-Security | AUSENTE | - |
| Content-Security-Policy | AUSENTE | - |
| X-Frame-Options | AUSENTE | - |
| X-Content-Type-Options | AUSENTE | - |
| Referrer-Policy | AUSENTE | - |
| Permissions-Policy | AUSENTE | - |

## Escaneo de puertos (Nmap) (2)

- **Comando:** `nmap -sV app.notap.io`
- **Host:** `69.46.46.120`
- **Hostnames:** app.notap.io

| Puerto | Estado | Servicio | Versión |
| --- | --- | --- | --- |
| 80/tcp | open | http | Pingora |
| 443/tcp | open | https | Pingora |

## Nmap NSE dirigido (35)

- **Comando:** `/usr/bin/nmap -sV --script default,vuln,safe -p 80,443 -oX - --script-args "http.useragent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36" app.notap.io`

| Puerto | Servicio | Script | Salida |
| --- | --- | --- | --- |
| 80/tcp | http | http-date | Mon, 08 Jun 2026 21:06:21 GMT; -1s from local time. |
| 80/tcp | http | http-useragent-tester | Status for browser useragent: 404<br>  Redirected To: https://app.notap.io/<br>  Allowed User Agents: <br>    Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36<br>    libwww<br>    lwp-trivial<br>    libcurl-agent/1.0<br>    PHP/<br>    Python-urllib/2.5<br>    GT::WWW<br>    Snoopy<br>    MFC_Tear_Sample<br>    HTTP::Lite<br>    PHPCrawl<br>    URI::Fetch<br>    Zend_Http_Client<br>    http client<br>    PECL::HTTP<br>    Wget/1.13.4 (linux-gnu)<br>    WWW-Mechanize/1.34 |
| 80/tcp | http | http-stored-xss | Couldn't find any stored XSS vulnerabilities. |
| 80/tcp | http | http-dombased-xss | Couldn't find any DOM based XSS. |
| 80/tcp | http | http-comments-displayer | Couldn't find any comments. |
| 80/tcp | http | http-referer-checker | Couldn't find any cross-domain scripts. |
| 80/tcp | http | http-title | Did not follow redirect to https://app.notap.io/ |
| 80/tcp | http | http-mobileversion-checker | No mobile version detected. |
| 80/tcp | http | http-server-header | Pingora |
| 80/tcp | http | http-xssed | No previously reported XSS vuln. |
| 80/tcp | http | http-csrf | Couldn't find any CSRF vulnerabilities. |
| 80/tcp | http | fingerprint-strings | DNSStatusRequestTCP, DNSVersionBindReqTCP, Help, RPCCheck: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:50 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close<br>  FourOhFourRequest: <br>    HTTP/1.1 301 Moved Permanently<br>    location: https://localhost/nice%20ports%2C/Tri%6Eity.txt%2ebak<br>    content-length: 0<br>    x-railway-67: 67<br>    Date: Mon, 08 Jun 2026 21:05:44 GMT<br>    Connection: close<br>  GetRequest, HTTPOptions: <br>    HTTP/1.1 301 Moved Permanently<br>    location: https://localhost/<br>    content-length: 0<br>    x-railway-67: 67<br>    Date: Mon, 08 Jun 2026 21:05:44 GMT<br>    Connection: close<br>  RTSPRequest, X11Probe: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:44 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close |
| 80/tcp | http | http-headers | location: https://app.notap.io/<br>  content-length: 0<br>  x-railway-67: 67<br>  Date: Mon, 08 Jun 2026 21:06:21 GMT<br>  Connection: close<br>  <br>  (Request type: GET) |
| 80/tcp | http | http-fetch | Please enter the complete path of the directory to save data in. |
| 80/tcp | http | http-slowloris-check | VULNERABLE:<br>  Slowloris DOS attack<br>    State: LIKELY VULNERABLE<br>    IDs:  CVE:CVE-2007-6750<br>      Slowloris tries to keep many connections to the target web server open and hold<br>      them open as long as possible.  It accomplishes this by opening connections to<br>      the target web server and sending a partial request. By doing so, it starves<br>      the http server's resources causing Denial Of Service.<br>      <br>    Disclosure date: 2009-09-17<br>    References:<br>      http://ha.ckers.org/slowloris/<br>      https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2007-6750 |
| 80/tcp | http | http-security-headers | Cache_Control: <br>    Header: Cache-Control: public, max-age=5 |
| 443/tcp | https | http-dombased-xss | Couldn't find any DOM based XSS. |
| 443/tcp | https | ssl-cert | Subject: commonName=app.notap.io<br>Subject Alternative Name: DNS:app.notap.io<br>Not valid before: 2026-05-01T23:29:37<br>Not valid after:  2026-07-30T23:29:36 |
| 443/tcp | https | http-date | Mon, 08 Jun 2026 21:06:16 GMT; -2s from local time. |
| 443/tcp | https | http-referer-checker | Couldn't find any cross-domain scripts. |
| 443/tcp | https | http-useragent-tester | Status for browser useragent: 404<br>  Allowed User Agents: <br>    Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36<br>    libwww<br>    lwp-trivial<br>    libcurl-agent/1.0<br>    PHP/<br>    Python-urllib/2.5<br>    GT::WWW<br>    Snoopy<br>    MFC_Tear_Sample<br>    HTTP::Lite<br>    PHPCrawl<br>    URI::Fetch<br>    Zend_Http_Client<br>    http client<br>    PECL::HTTP<br>    Wget/1.13.4 (linux-gnu)<br>    WWW-Mechanize/1.34 |
| 443/tcp | https | http-stored-xss | Couldn't find any stored XSS vulnerabilities. |
| 443/tcp | https | http-mobileversion-checker | No mobile version detected. |
| 443/tcp | https | fingerprint-strings | DNSStatusRequestTCP, DNSVersionBindReqTCP, Help, RPCCheck: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:57 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close<br>  RTSPRequest: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:56 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close<br>  SSLSessionReq, TLSSessionReq, TerminalServerCookie: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:58 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close<br>  tor-versions: <br>    HTTP/1.1 400 Bad Request<br>    Server: Pingora<br>    Date: Mon, 08 Jun 2026 21:05:51 GMT<br>    Content-Length: 0<br>    Cache-Control: private, no-store<br>    Connection: close |
| 443/tcp | https | http-title | Site doesn't have a title (application/json). |
| 443/tcp | https | http-headers | Cache-Control: public, max-age=5<br>  Content-Type: application/json<br>  Server: railway-hikari<br>  x-railway-edge: railway/us-east4-eqdc4a<br>  x-railway-fallback: true<br>  x-railway-request-id: QbyGzsveRn2n9C3DFFmdQQ<br>  Content-Length: 101<br>  Date: Mon, 08 Jun 2026 21:06:19 GMT<br>  x-hikari-trace: atl1.mwdw<br>  Connection: close<br>  <br>  (Request type: GET) |
| 443/tcp | https | http-fetch | Please enter the complete path of the directory to save data in. |
| 443/tcp | https | http-csrf | Couldn't find any CSRF vulnerabilities. |
| 443/tcp | https | http-xssed | No previously reported XSS vuln. |
| 443/tcp | https | http-security-headers | Strict_Transport_Security: <br>    HSTS not configured in HTTPS Server<br>  Cache_Control: <br>    Header: Cache-Control: public, max-age=5 |
| 443/tcp | https | http-vuln-cve2014-3704 | ERROR: Script execution failed (use -d to debug) |
| 443/tcp | https | ssl-date | TLS randomness does not represent time |
| 443/tcp | https | http-server-header | Pingora<br>  railway-hikari |
| 443/tcp | https | http-comments-displayer | Couldn't find any comments. |
| 443/tcp | https | http-aspnet-debug | ERROR: Script execution failed (use -d to debug) |

## Spidering

| Métrica | Valor |
| --- | --- |
| URLs totales | 1 |
| Parámetros únicos | 0 |
| Formularios | 0 |

### URLs descubiertas (1)

| URL |
| --- |
| http://app.notap.io |

## Análisis de código fuente

| Métrica | Valor |
| --- | --- |
| Páginas analizadas | 0 |
| Recursos JS/JSON analizados | 0 |
| Hallazgos totales | 0 |
| Critical | 0 |
| High | 0 |
| Medium | 0 |
| Low | 0 |

## Active Directory

| Campo | Valor |
| --- | --- |
| Domain Controller | app.notap.io |
| Dominio | notap.io |
| Base DN | DC=notap,DC=io |
| Modo | anonymous |
| Kerbrute usuarios validos | 0 |
| AS-REP roastable | 0 |
| Kerberoastable SPNs | 0 |
| LDAP usuarios | 0 |
| LDAP grupos | 0 |
| LDAP equipos | 0 |
| NXC credenciales | 0 |

### Salida bruta de herramientas AD

#### ldapsearch users

- **Comando:** `/usr/bin/ldapsearch -x -LLL -H ldap://app.notap.io -b DC=notap,DC=io (&(objectCategory=person)(objectClass=user)) sAMAccountName userPrincipalName cn displayName memberOf userAccountControl pwdLastSet lastLogonTimestamp`
- **Return code:** `255`

```text
ldap_sasl_bind(SIMPLE): Can't contact LDAP server (-1)
```

#### ldapsearch groups

- **Comando:** `/usr/bin/ldapsearch -x -LLL -H ldap://app.notap.io -b DC=notap,DC=io (objectClass=group) cn description member`
- **Return code:** `255`

```text
ldap_sasl_bind(SIMPLE): Can't contact LDAP server (-1)
```

#### ldapsearch computers

- **Comando:** `/usr/bin/ldapsearch -x -LLL -H ldap://app.notap.io -b DC=notap,DC=io (objectClass=computer) dNSHostName sAMAccountName operatingSystem operatingSystemVersion lastLogonTimestamp`
- **Return code:** `255`

```text
ldap_sasl_bind(SIMPLE): Can't contact LDAP server (-1)
```

#### impacket-GetNPUsers AS-REP

- **Comando:** `/usr/bin/impacket-GetNPUsers notap.io/ -usersfile /usr/share/seclists/Usernames/top-usernames-shortlist.txt -dc-ip app.notap.io -format hashcat -outputfile /home/kali/WSTG-Scan/reports/active_directory/notap.io_app.notap.io/asrep_hashes.txt`
- **Return code:** `1`

```text
Traceback (most recent call last):
  File "/usr/share/doc/python3-impacket/examples/GetNPUsers.py", line 38, in <module>
    from pyasn1.codec.der import decoder, encoder
ModuleNotFoundError: No module named 'pyasn1'
```

#### nxc smb enum

- **Comando:** `/usr/bin/nxc smb app.notap.io -d notap.io -u  -p *** --users --groups --shares --pass-pol`
- **Return code:** `0`

```text
[*] First time use detected
[*] Creating home directory structure
[*] Creating missing folder logs
[*] Creating missing folder modules
[*] Creating missing folder workspaces
[*] Creating missing folder obfuscated_scripts
[*] Creating missing folder screenshots
[*] Creating missing folder logs/sam
[*] Creating missing folder logs/lsa
[*] Creating missing folder logs/ntds
[*] Creating missing folder logs/dpapi
[*] Creating default workspace
[*] Initializing WINRM protocol database
[*] Initializing NFS protocol database
[*] Initializing SSH protocol database
[*] Initializing MSSQL protocol database
[*] Initializing VNC protocol database
[*] Initializing LDAP protocol database
[*] Initializing WMI protocol database
[*] Initializing RDP protocol database
[*] Initializing SMB protocol database
[*] Initializing FTP protocol database
[*] Copying default configuration file
```

#### nxc smb bruteforce

- **Comando:** `/usr/bin/nxc smb app.notap.io -d notap.io -u /usr/share/seclists/Usernames/top-usernames-shortlist.txt -p *** --continue-on-success`
- **Return code:** `0`

```text
-
```

## Endpoints API descubiertos (1330)

| Status | Endpoint | Content-Type |
| --- | --- | --- |
| 301 | /api | - |
| 301 | /api/v1 | - |
| 301 | /api/v2 | - |
| 301 | /api/v3 | - |
| 301 | /v1 | - |
| 301 | /v2 | - |
| 301 | /v3 | - |
| 301 | /rest | - |
| 301 | /rest/v1 | - |
| 301 | /api/users | - |
| 301 | /api/user | - |
| 301 | /api/accounts | - |
| 301 | /api/account | - |
| 301 | /api/admin | - |
| 301 | /api/me | - |
| 301 | /api/profile | - |
| 301 | /api/whoami | - |
| 301 | /api/config | - |
| 301 | /api/settings | - |
| 301 | /api/flags | - |
| 301 | /api/data | - |
| 301 | /api/keys | - |
| 301 | /api/tokens | - |
| 301 | /api/secrets | - |
| 301 | /api/credentials | - |
| 301 | /api/debug | - |
| 301 | /api/test | - |
| 301 | /api/internal | - |
| 301 | /rest/users | - |
| 301 | /rest/user | - |
| 301 | /rest/admin | - |
| 301 | /rest/profile | - |
| 301 | /swagger | - |
| 301 | /swagger-ui.html | - |
| 301 | /swagger-ui/ | - |
| 301 | /swagger.json | - |
| 301 | /swagger.yaml | - |
| 301 | /openapi.json | - |
| 301 | /openapi.yaml | - |
| 301 | /api-docs | - |
| 301 | /v2/api-docs | - |
| 301 | /v3/api-docs | - |
| 301 | /redoc | - |
| 301 | /docs | - |
| 301 | /api/docs | - |
| 301 | /api/swagger | - |
| 301 | /graphql | - |
| 301 | /graphiql | - |
| 301 | /api/graphql | - |
| 301 | /query | - |
| 301 | /api/query | - |
| 301 | /actuator | - |
| 301 | /actuator/env | - |
| 301 | /actuator/health | - |
| 301 | /actuator/mappings | - |
| 301 | /actuator/beans | - |
| 301 | /actuator/httptrace | - |
| 301 | /actuator/loggers | - |
| 301 | /health | - |
| 301 | /metrics | - |
| 301 | /info | - |
| 301 | /status | - |
| 301 | /ping | - |
| 301 | /api/auth | - |
| 301 | /api/login | - |
| 301 | /api/token | - |
| 301 | /api/refresh | - |
| 301 | /api/register | - |
| 301 | /api/signup | - |
| 301 | /.well-known/ | - |
| 301 | /api/version | - |
| 301 | /api/changelog | - |
| 301 | /console | - |
| 301 | /api/console | - |
| 301 | /h2-console | - |
| 301 | /api/logout | - |
| 301 | /api/mfa | - |
| 301 | /api/signin | - |
| 301 | /api/reset-password | - |
| 301 | /api/sessions | - |
| 301 | /api/2fa | - |
| 301 | /api/forgot-password | - |
| 301 | /api/password | - |
| 301 | /api/session | - |
| 301 | /api/otp | - |
| 301 | /api/permissions | - |
| 301 | /api/feature-flags | - |
| 301 | /api/roles | - |
| 301 | /api/groups | - |
| 301 | /api/audit | - |
| 301 | /api/privileges | - |
| 301 | /api/audit-log | - |
| 301 | /api/events | - |
| 301 | /api/logs | - |
| 301 | /api/items | - |
| 301 | /api/products | - |
| 301 | /api/orders | - |
| 301 | /api/invoices | - |
| 301 | /api/payments | - |
| 301 | /api/transactions | - |
| 301 | /api/transfer | - |
| 301 | /api/transfers | - |
| 301 | /api/balance | - |
| 301 | /api/wallets | - |
| 301 | /api/plans | - |
| 301 | /api/subscriptions | - |
| 301 | /api/billing | - |
| 301 | /api/cart | - |
| 301 | /api/checkout | - |
| 301 | /api/notes | - |
| 301 | /api/messages | - |
| 301 | /api/comments | - |
| 301 | /api/chats | - |
| 301 | /api/posts | - |
| 301 | /api/articles | - |
| 301 | /api/files | - |
| 301 | /api/documents | - |
| 301 | /api/uploads | - |
| 301 | /api/images | - |
| 301 | /api/media | - |
| 301 | /api/attachments | - |
| 301 | /api/search | - |
| 301 | /api/filter | - |
| 301 | /api/tags | - |
| 301 | /api/categories | - |
| 301 | /api/stats | - |
| 301 | /api/health | - |
| 301 | /api/status | - |
| 301 | /api/metrics | - |
| 301 | /api/private | - |
| 301 | /api/info | - |
| 301 | /api/hidden | - |
| 301 | /api/api-keys | - |
| 301 | /api/export | - |
| 301 | /api/import | - |
| 301 | /api/backup | - |
| 301 | /api/dump | - |
| 301 | /api/report | - |
| 301 | /api/reports | - |
| 301 | /api/notifications | - |
| 301 | /api/webhooks | - |
| 301 | /api/callbacks | - |
| 301 | /api/subscribe | - |
| 301 | /api/feed | - |
| 301 | /api/activity | - |
| 301 | /api/feeds | - |
| 301 | /api/history | - |
| 301 | /api/v1/users | - |
| 301 | /api/v1/user | - |
| 301 | /api/v1/accounts | - |
| 301 | /api/v1/account | - |
| 301 | /api/v1/me | - |
| 301 | /api/v1/whoami | - |
| 301 | /api/v1/auth | - |
| 301 | /api/v1/profile | - |
| 301 | /api/v1/login | - |
| 301 | /api/v1/logout | - |
| 301 | /api/v1/register | - |
| 301 | /api/v1/signup | - |
| 301 | /api/v1/signin | - |
| 301 | /api/v1/token | - |
| 301 | /api/v1/refresh | - |
| 301 | /api/v1/tokens | - |
| 301 | /api/v1/session | - |
| 301 | /api/v1/sessions | - |
| 301 | /api/v1/password | - |
| 301 | /api/v1/reset-password | - |
| 301 | /api/v1/forgot-password | - |
| 301 | /api/v1/2fa | - |
| 301 | /api/v1/mfa | - |
| 301 | /api/v1/otp | - |
| 301 | /api/v1/admin | - |
| 301 | /api/v1/config | - |
| 301 | /api/v1/settings | - |
| 301 | /api/v1/flags | - |
| 301 | /api/v1/feature-flags | - |
| 301 | /api/v1/permissions | - |
| 301 | /api/v1/roles | - |
| 301 | /api/v1/groups | - |
| 301 | /api/v1/privileges | - |
| 301 | /api/v1/audit | - |
| 301 | /api/v1/audit-log | - |
| 301 | /api/v1/logs | - |
| 301 | /api/v1/events | - |
| 301 | /api/v1/data | - |
| 301 | /api/v1/items | - |
| 301 | /api/v1/products | - |
| 301 | /api/v1/orders | - |
| 301 | /api/v1/invoices | - |
| 301 | /api/v1/payments | - |
| 301 | /api/v1/transactions | - |
| 301 | /api/v1/transfer | - |
| 301 | /api/v1/transfers | - |
| 301 | /api/v1/balance | - |
| 301 | /api/v1/wallets | - |
| 301 | /api/v1/subscriptions | - |
| 301 | /api/v1/plans | - |
| 301 | /api/v1/billing | - |
| 301 | /api/v1/cart | - |
| 301 | /api/v1/checkout | - |
| 301 | /api/v1/notes | - |
| 301 | /api/v1/messages | - |
| 301 | /api/v1/chats | - |
| 301 | /api/v1/comments | - |
| 301 | /api/v1/posts | - |
| 301 | /api/v1/articles | - |
| 301 | /api/v1/files | - |
| 301 | /api/v1/uploads | - |
| 301 | /api/v1/documents | - |
| 301 | /api/v1/attachments | - |
| 301 | /api/v1/media | - |
| 301 | /api/v1/images | - |
| 301 | /api/v1/search | - |
| 301 | /api/v1/filter | - |
| 301 | /api/v1/query | - |
| 301 | /api/v1/tags | - |
| 301 | /api/v1/categories | - |
| 301 | /api/v1/stats | - |
| 301 | /api/v1/metrics | - |
| 301 | /api/v1/health | - |
| 301 | /api/v1/version | - |
| 301 | /api/v1/status | - |
| 301 | /api/v1/info | - |
| 301 | /api/v1/debug | - |
| 301 | /api/v1/test | - |
| 301 | /api/v1/internal | - |
| 301 | /api/v1/private | - |
| 301 | /api/v1/hidden | - |
| 301 | /api/v1/keys | - |
| 301 | /api/v1/secrets | - |
| 301 | /api/v1/credentials | - |
| 301 | /api/v1/api-keys | - |
| 301 | /api/v1/export | - |
| 301 | /api/v1/import | - |
| 301 | /api/v1/backup | - |
| 301 | /api/v1/report | - |
| 301 | /api/v1/dump | - |
| 301 | /api/v1/reports | - |
| 301 | /api/v1/notifications | - |
| 301 | /api/v1/webhooks | - |
| 301 | /api/v1/callbacks | - |
| 301 | /api/v1/subscribe | - |
| 301 | /api/v1/feed | - |
| 301 | /api/v1/feeds | - |
| 301 | /api/v1/activity | - |
| 301 | /api/v1/history | - |
| 301 | /api/v2/users | - |
| 301 | /api/v2/user | - |
| 301 | /api/v2/accounts | - |
| 301 | /api/v2/account | - |
| 301 | /api/v2/me | - |
| 301 | /api/v2/profile | - |
| 301 | /api/v2/whoami | - |
| 301 | /api/v2/login | - |
| 301 | /api/v2/auth | - |
| 301 | /api/v2/logout | - |
| 301 | /api/v2/register | - |
| 301 | /api/v2/signup | - |
| 301 | /api/v2/signin | - |
| 301 | /api/v2/tokens | - |
| 301 | /api/v2/token | - |
| 301 | /api/v2/refresh | - |
| 301 | /api/v2/session | - |
| 301 | /api/v2/sessions | - |
| 301 | /api/v2/password | - |
| 301 | /api/v2/reset-password | - |
| 301 | /api/v2/forgot-password | - |
| 301 | /api/v2/mfa | - |
| 301 | /api/v2/otp | - |
| 301 | /api/v2/2fa | - |
| 301 | /api/v2/config | - |
| 301 | /api/v2/settings | - |
| 301 | /api/v2/admin | - |
| 301 | /api/v2/flags | - |
| 301 | /api/v2/feature-flags | - |
| 301 | /api/v2/permissions | - |
| 301 | /api/v2/roles | - |
| 301 | /api/v2/privileges | - |
| 301 | /api/v2/groups | - |
| 301 | /api/v2/audit | - |
| 301 | /api/v2/audit-log | - |
| 301 | /api/v2/logs | - |
| 301 | /api/v2/events | - |
| 301 | /api/v2/data | - |
| 301 | /api/v2/items | - |
| 301 | /api/v2/orders | - |
| 301 | /api/v2/products | - |
| 301 | /api/v2/invoices | - |
| 301 | /api/v2/payments | - |
| 301 | /api/v2/transactions | - |
| 301 | /api/v2/transfer | - |
| 301 | /api/v2/transfers | - |
| 301 | /api/v2/wallets | - |
| 301 | /api/v2/balance | - |
| 301 | /api/v2/plans | - |
| 301 | /api/v2/subscriptions | - |
| 301 | /api/v2/billing | - |
| 301 | /api/v2/cart | - |
| 301 | /api/v2/checkout | - |
| 301 | /api/v2/notes | - |
| 301 | /api/v2/messages | - |
| 301 | /api/v2/chats | - |
| 301 | /api/v2/comments | - |
| 301 | /api/v2/posts | - |
| 301 | /api/v2/articles | - |
| 301 | /api/v2/files | - |
| 301 | /api/v2/uploads | - |
| 301 | /api/v2/attachments | - |
| 301 | /api/v2/documents | - |
| 301 | /api/v2/media | - |
| 301 | /api/v2/images | - |
| 301 | /api/v2/search | - |
| 301 | /api/v2/filter | - |
| 301 | /api/v2/query | - |
| 301 | /api/v2/categories | - |
| 301 | /api/v2/stats | - |
| 301 | /api/v2/tags | - |
| 301 | /api/v2/metrics | - |
| 301 | /api/v2/status | - |
| 301 | /api/v2/health | - |
| 301 | /api/v2/version | - |
| 301 | /api/v2/private | - |
| 301 | /api/v2/info | - |
| 301 | /api/v2/debug | - |
| 301 | /api/v2/test | - |
| 301 | /api/v2/hidden | - |
| 301 | /api/v2/internal | - |
| 301 | /api/v2/keys | - |
| 301 | /api/v2/secrets | - |
| 301 | /api/v2/credentials | - |
| 301 | /api/v2/export | - |
| 301 | /api/v2/api-keys | - |
| 301 | /api/v2/backup | - |
| 301 | /api/v2/report | - |
| 301 | /api/v2/import | - |
| 301 | /api/v2/dump | - |
| 301 | /api/v2/reports | - |
| 301 | /api/v2/notifications | - |
| 301 | /api/v2/webhooks | - |
| 301 | /api/v2/callbacks | - |
| 301 | /api/v2/subscribe | - |
| 301 | /api/v2/feed | - |
| 301 | /api/v2/feeds | - |
| 301 | /api/v2/activity | - |
| 301 | /api/v2/history | - |
| 301 | /api/v3/users | - |
| 301 | /api/v3/user | - |
| 301 | /api/v3/accounts | - |
| 301 | /api/v3/account | - |
| 301 | /api/v3/me | - |
| 301 | /api/v3/profile | - |
| 301 | /api/v3/auth | - |
| 301 | /api/v3/whoami | - |
| 301 | /api/v3/login | - |
| 301 | /api/v3/logout | - |
| 301 | /api/v3/signup | - |
| 301 | /api/v3/register | - |
| 301 | /api/v3/signin | - |
| 301 | /api/v3/token | - |
| 301 | /api/v3/tokens | - |
| 301 | /api/v3/refresh | - |
| 301 | /api/v3/session | - |
| 301 | /api/v3/sessions | - |
| 301 | /api/v3/reset-password | - |
| 301 | /api/v3/password | - |
| 301 | /api/v3/mfa | - |
| 301 | /api/v3/otp | - |
| 301 | /api/v3/forgot-password | - |
| 301 | /api/v3/2fa | - |
| 301 | /api/v3/admin | - |
| 301 | /api/v3/config | - |
| 301 | /api/v3/settings | - |
| 301 | /api/v3/flags | - |
| 301 | /api/v3/feature-flags | - |
| 301 | /api/v3/roles | - |
| 301 | /api/v3/permissions | - |
| 301 | /api/v3/groups | - |
| 301 | /api/v3/audit | - |
| 301 | /api/v3/privileges | - |
| 301 | /api/v3/audit-log | - |
| 301 | /api/v3/logs | - |
| 301 | /api/v3/events | - |
| 301 | /api/v3/data | - |
| 301 | /api/v3/items | - |
| 301 | /api/v3/products | - |
| 301 | /api/v3/orders | - |
| 301 | /api/v3/invoices | - |
| 301 | /api/v3/payments | - |
| 301 | /api/v3/transactions | - |
| 301 | /api/v3/transfer | - |
| 301 | /api/v3/wallets | - |
| 301 | /api/v3/transfers | - |
| 301 | /api/v3/balance | - |
| 301 | /api/v3/plans | - |
| 301 | /api/v3/subscriptions | - |
| 301 | /api/v3/billing | - |
| 301 | /api/v3/cart | - |
| 301 | /api/v3/checkout | - |
| 301 | /api/v3/notes | - |
| 301 | /api/v3/messages | - |
| 301 | /api/v3/chats | - |
| 301 | /api/v3/comments | - |
| 301 | /api/v3/files | - |
| 301 | /api/v3/posts | - |
| 301 | /api/v3/articles | - |
| 301 | /api/v3/uploads | - |
| 301 | /api/v3/documents | - |
| 301 | /api/v3/attachments | - |
| 301 | /api/v3/media | - |
| 301 | /api/v3/images | - |
| 301 | /api/v3/search | - |
| 301 | /api/v3/filter | - |
| 301 | /api/v3/query | - |
| 301 | /api/v3/tags | - |
| 301 | /api/v3/stats | - |
| 301 | /api/v3/categories | - |
| 301 | /api/v3/metrics | - |
| 301 | /api/v3/health | - |
| 301 | /api/v3/status | - |
| 301 | /api/v3/version | - |
| 301 | /api/v3/info | - |
| 301 | /api/v3/debug | - |
| 301 | /api/v3/internal | - |
| 301 | /api/v3/test | - |
| 301 | /api/v3/private | - |
| 301 | /api/v3/hidden | - |
| 301 | /api/v3/keys | - |
| 301 | /api/v3/secrets | - |
| 301 | /api/v3/credentials | - |
| 301 | /api/v3/api-keys | - |
| 301 | /api/v3/import | - |
| 301 | /api/v3/export | - |
| 301 | /api/v3/backup | - |
| 301 | /api/v3/dump | - |
| 301 | /api/v3/report | - |
| 301 | /api/v3/reports | - |
| 301 | /api/v3/notifications | - |
| 301 | /api/v3/webhooks | - |
| 301 | /api/v3/callbacks | - |
| 301 | /api/v3/subscribe | - |
| 301 | /api/v3/feed | - |
| 301 | /api/v3/feeds | - |
| 301 | /api/v3/activity | - |
| 301 | /api/v3/history | - |
| 301 | /v1/users | - |
| 301 | /v1/user | - |
| 301 | /v1/accounts | - |
| 301 | /v1/account | - |
| 301 | /v1/me | - |
| 301 | /v1/profile | - |
| 301 | /v1/whoami | - |
| 301 | /v1/auth | - |
| 301 | /v1/login | - |
| 301 | /v1/logout | - |
| 301 | /v1/register | - |
| 301 | /v1/signup | - |
| 301 | /v1/signin | - |
| 301 | /v1/token | - |
| 301 | /v1/tokens | - |
| 301 | /v1/refresh | - |
| 301 | /v1/session | - |
| 301 | /v1/sessions | - |
| 301 | /v1/password | - |
| 301 | /v1/reset-password | - |
| 301 | /v1/forgot-password | - |
| 301 | /v1/2fa | - |
| 301 | /v1/mfa | - |
| 301 | /v1/otp | - |
| 301 | /v1/admin | - |
| 301 | /v1/config | - |
| 301 | /v1/flags | - |
| 301 | /v1/permissions | - |
| 301 | /v1/feature-flags | - |
| 301 | /v1/settings | - |
| 301 | /v1/roles | - |
| 301 | /v1/privileges | - |
| 301 | /v1/groups | - |
| 301 | /v1/audit | - |
| 301 | /v1/audit-log | - |
| 301 | /v1/logs | - |
| 301 | /v1/events | - |
| 301 | /v1/data | - |
| 301 | /v1/items | - |
| 301 | /v1/products | - |
| 301 | /v1/orders | - |
| 301 | /v1/invoices | - |
| 301 | /v1/payments | - |
| 301 | /v1/transfers | - |
| 301 | /v1/transactions | - |
| 301 | /v1/wallets | - |
| 301 | /v1/transfer | - |
| 301 | /v1/balance | - |
| 301 | /v1/subscriptions | - |
| 301 | /v1/billing | - |
| 301 | /v1/plans | - |
| 301 | /v1/checkout | - |
| 301 | /v1/notes | - |
| 301 | /v1/cart | - |
| 301 | /v1/messages | - |
| 301 | /v1/chats | - |
| 301 | /v1/comments | - |
| 301 | /v1/posts | - |
| 301 | /v1/articles | - |
| 301 | /v1/files | - |
| 301 | /v1/uploads | - |
| 301 | /v1/documents | - |
| 301 | /v1/attachments | - |
| 301 | /v1/media | - |
| 301 | /v1/images | - |
| 301 | /v1/search | - |
| 301 | /v1/filter | - |
| 301 | /v1/query | - |
| 301 | /v1/tags | - |
| 301 | /v1/categories | - |
| 301 | /v1/stats | - |
| 301 | /v1/metrics | - |
| 301 | /v1/health | - |
| 301 | /v1/status | - |
| 301 | /v1/version | - |
| 301 | /v1/info | - |
| 301 | /v1/debug | - |
| 301 | /v1/test | - |
| 301 | /v1/internal | - |
| 301 | /v1/private | - |
| 301 | /v1/hidden | - |
| 301 | /v1/keys | - |
| 301 | /v1/secrets | - |
| 301 | /v1/credentials | - |
| 301 | /v1/api-keys | - |
| 301 | /v1/export | - |
| 301 | /v1/import | - |
| 301 | /v1/backup | - |
| 301 | /v1/dump | - |
| 301 | /v1/report | - |
| 301 | /v1/reports | - |
| 301 | /v1/notifications | - |
| 301 | /v1/webhooks | - |
| 301 | /v1/callbacks | - |
| 301 | /v1/subscribe | - |
| 301 | /v1/feed | - |
| 301 | /v1/feeds | - |
| 301 | /v1/activity | - |
| 301 | /v1/history | - |
| 301 | /v2/user | - |
| 301 | /v2/accounts | - |
| 301 | /v2/users | - |
| 301 | /v2/account | - |
| 301 | /v2/me | - |
| 301 | /v2/profile | - |
| 301 | /v2/whoami | - |
| 301 | /v2/logout | - |
| 301 | /v2/auth | - |
| 301 | /v2/register | - |
| 301 | /v2/login | - |
| 301 | /v2/signup | - |
| 301 | /v2/signin | - |
| 301 | /v2/token | - |
| 301 | /v2/tokens | - |
| 301 | /v2/refresh | - |
| 301 | /v2/session | - |
| 301 | /v2/sessions | - |
| 301 | /v2/password | - |
| 301 | /v2/reset-password | - |
| 301 | /v2/forgot-password | - |
| 301 | /v2/2fa | - |
| 301 | /v2/mfa | - |
| 301 | /v2/otp | - |
| 301 | /v2/admin | - |
| 301 | /v2/config | - |
| 301 | /v2/settings | - |
| 301 | /v2/flags | - |
| 301 | /v2/feature-flags | - |
| 301 | /v2/permissions | - |
| 301 | /v2/roles | - |
| 301 | /v2/groups | - |
| 301 | /v2/privileges | - |
| 301 | /v2/audit | - |
| 301 | /v2/audit-log | - |
| 301 | /v2/logs | - |
| 301 | /v2/events | - |
| 301 | /v2/data | - |
| 301 | /v2/items | - |
| 301 | /v2/products | - |
| 301 | /v2/orders | - |
| 301 | /v2/invoices | - |
| 301 | /v2/payments | - |
| 301 | /v2/transactions | - |
| 301 | /v2/transfer | - |
| 301 | /v2/transfers | - |
| 301 | /v2/wallets | - |
| 301 | /v2/balance | - |
| 301 | /v2/subscriptions | - |
| 301 | /v2/plans | - |
| 301 | /v2/billing | - |
| 301 | /v2/cart | - |
| 301 | /v2/checkout | - |
| 301 | /v2/notes | - |
| 301 | /v2/messages | - |
| 301 | /v2/chats | - |
| 301 | /v2/comments | - |
| 301 | /v2/posts | - |
| 301 | /v2/articles | - |
| 301 | /v2/files | - |
| 301 | /v2/uploads | - |
| 301 | /v2/documents | - |
| 301 | /v2/attachments | - |
| 301 | /v2/media | - |
| 301 | /v2/images | - |
| 301 | /v2/search | - |
| 301 | /v2/filter | - |
| 301 | /v2/query | - |
| 301 | /v2/tags | - |
| 301 | /v2/categories | - |
| 301 | /v2/stats | - |
| 301 | /v2/metrics | - |
| 301 | /v2/health | - |
| 301 | /v2/status | - |
| 301 | /v2/version | - |
| 301 | /v2/info | - |
| 301 | /v2/debug | - |
| 301 | /v2/test | - |
| 301 | /v2/internal | - |
| 301 | /v2/private | - |
| 301 | /v2/hidden | - |
| 301 | /v2/keys | - |
| 301 | /v2/credentials | - |
| 301 | /v2/secrets | - |
| 301 | /v2/api-keys | - |
| 301 | /v2/export | - |
| 301 | /v2/import | - |
| 301 | /v2/backup | - |
| 301 | /v2/dump | - |
| 301 | /v2/report | - |
| 301 | /v2/notifications | - |
| 301 | /v2/reports | - |
| 301 | /v2/webhooks | - |
| 301 | /v2/callbacks | - |
| 301 | /v2/subscribe | - |
| 301 | /v2/feed | - |
| 301 | /v2/feeds | - |
| 301 | /v2/activity | - |
| 301 | /v2/history | - |
| 301 | /v3/users | - |
| 301 | /v3/user | - |
| 301 | /v3/accounts | - |
| 301 | /v3/account | - |
| 301 | /v3/me | - |
| 301 | /v3/profile | - |
| 301 | /v3/whoami | - |
| 301 | /v3/auth | - |
| 301 | /v3/login | - |
| 301 | /v3/logout | - |
| 301 | /v3/register | - |
| 301 | /v3/signup | - |
| 301 | /v3/signin | - |
| 301 | /v3/token | - |
| 301 | /v3/tokens | - |
| 301 | /v3/refresh | - |
| 301 | /v3/session | - |
| 301 | /v3/sessions | - |
| 301 | /v3/password | - |
| 301 | /v3/reset-password | - |
| 301 | /v3/forgot-password | - |
| 301 | /v3/mfa | - |
| 301 | /v3/2fa | - |
| 301 | /v3/otp | - |
| 301 | /v3/admin | - |
| 301 | /v3/config | - |
| 301 | /v3/settings | - |
| 301 | /v3/flags | - |
| 301 | /v3/feature-flags | - |
| 301 | /v3/permissions | - |
| 301 | /v3/roles | - |
| 301 | /v3/groups | - |
| 301 | /v3/privileges | - |
| 301 | /v3/audit | - |
| 301 | /v3/audit-log | - |
| 301 | /v3/logs | - |
| 301 | /v3/events | - |
| 301 | /v3/data | - |
| 301 | /v3/items | - |
| 301 | /v3/products | - |
| 301 | /v3/orders | - |
| 301 | /v3/invoices | - |
| 301 | /v3/payments | - |
| 301 | /v3/transactions | - |
| 301 | /v3/transfer | - |
| 301 | /v3/transfers | - |
| 301 | /v3/wallets | - |
| 301 | /v3/balance | - |
| 301 | /v3/subscriptions | - |
| 301 | /v3/plans | - |
| 301 | /v3/billing | - |
| 301 | /v3/cart | - |
| 301 | /v3/checkout | - |
| 301 | /v3/notes | - |
| 301 | /v3/messages | - |
| 301 | /v3/chats | - |
| 301 | /v3/comments | - |
| 301 | /v3/posts | - |
| 301 | /v3/articles | - |
| 301 | /v3/files | - |
| 301 | /v3/uploads | - |
| 301 | /v3/documents | - |
| 301 | /v3/attachments | - |
| 301 | /v3/media | - |
| 301 | /v3/images | - |
| 301 | /v3/search | - |
| 301 | /v3/filter | - |
| 301 | /v3/query | - |
| 301 | /v3/tags | - |
| 301 | /v3/categories | - |
| 301 | /v3/stats | - |
| 301 | /v3/metrics | - |
| 301 | /v3/health | - |
| 301 | /v3/status | - |
| 301 | /v3/version | - |
| 301 | /v3/info | - |
| 301 | /v3/debug | - |
| 301 | /v3/test | - |
| 301 | /v3/internal | - |
| 301 | /v3/private | - |
| 301 | /v3/hidden | - |
| 301 | /v3/keys | - |
| 301 | /v3/secrets | - |
| 301 | /v3/credentials | - |
| 301 | /v3/api-keys | - |
| 301 | /v3/export | - |
| 301 | /v3/import | - |
| 301 | /v3/backup | - |
| 301 | /v3/dump | - |
| 301 | /v3/report | - |
| 301 | /v3/reports | - |
| 301 | /v3/notifications | - |
| 301 | /v3/webhooks | - |
| 301 | /v3/callbacks | - |
| 301 | /v3/subscribe | - |
| 301 | /v3/feed | - |
| 301 | /v3/feeds | - |
| 301 | /v3/activity | - |
| 301 | /v3/history | - |
| 301 | /rest/accounts | - |
| 301 | /rest/account | - |
| 301 | /rest/me | - |
| 301 | /rest/whoami | - |
| 301 | /rest/auth | - |
| 301 | /rest/login | - |
| 301 | /rest/logout | - |
| 301 | /rest/register | - |
| 301 | /rest/signup | - |
| 301 | /rest/signin | - |
| 301 | /rest/token | - |
| 301 | /rest/tokens | - |
| 301 | /rest/refresh | - |
| 301 | /rest/session | - |
| 301 | /rest/sessions | - |
| 301 | /rest/password | - |
| 301 | /rest/reset-password | - |
| 301 | /rest/forgot-password | - |
| 301 | /rest/2fa | - |
| 301 | /rest/mfa | - |
| 301 | /rest/otp | - |
| 301 | /rest/config | - |
| 301 | /rest/settings | - |
| 301 | /rest/flags | - |
| 301 | /rest/feature-flags | - |
| 301 | /rest/permissions | - |
| 301 | /rest/roles | - |
| 301 | /rest/groups | - |
| 301 | /rest/privileges | - |
| 301 | /rest/audit | - |
| 301 | /rest/audit-log | - |
| 301 | /rest/logs | - |
| 301 | /rest/events | - |
| 301 | /rest/data | - |
| 301 | /rest/items | - |
| 301 | /rest/products | - |
| 301 | /rest/orders | - |
| 301 | /rest/invoices | - |
| 301 | /rest/payments | - |
| 301 | /rest/transactions | - |
| 301 | /rest/transfer | - |
| 301 | /rest/transfers | - |
| 301 | /rest/wallets | - |
| 301 | /rest/balance | - |
| 301 | /rest/subscriptions | - |
| 301 | /rest/plans | - |
| 301 | /rest/billing | - |
| 301 | /rest/cart | - |
| 301 | /rest/checkout | - |
| 301 | /rest/notes | - |
| 301 | /rest/messages | - |
| 301 | /rest/chats | - |
| 301 | /rest/comments | - |
| 301 | /rest/posts | - |
| 301 | /rest/articles | - |
| 301 | /rest/files | - |
| 301 | /rest/uploads | - |
| 301 | /rest/attachments | - |
| 301 | /rest/documents | - |
| 301 | /rest/media | - |
| 301 | /rest/images | - |
| 301 | /rest/search | - |
| 301 | /rest/query | - |
| 301 | /rest/filter | - |
| 301 | /rest/tags | - |
| 301 | /rest/categories | - |
| 301 | /rest/stats | - |
| 301 | /rest/metrics | - |
| 301 | /rest/health | - |
| 301 | /rest/status | - |
| 301 | /rest/version | - |
| 301 | /rest/info | - |
| 301 | /rest/debug | - |
| 301 | /rest/test | - |
| 301 | /rest/internal | - |
| 301 | /rest/private | - |
| 301 | /rest/hidden | - |
| 301 | /rest/keys | - |
| 301 | /rest/secrets | - |
| 301 | /rest/credentials | - |
| 301 | /rest/api-keys | - |
| 301 | /rest/export | - |
| 301 | /rest/import | - |
| 301 | /rest/backup | - |
| 301 | /rest/dump | - |
| 301 | /rest/report | - |
| 301 | /rest/reports | - |
| 301 | /rest/notifications | - |
| 301 | /rest/webhooks | - |
| 301 | /rest/callbacks | - |
| 301 | /rest/subscribe | - |
| 301 | /rest/feed | - |
| 301 | /rest/feeds | - |
| 301 | /rest/activity | - |
| 301 | /rest/history | - |
| 301 | /rest/v1/users | - |
| 301 | /rest/v1/user | - |
| 301 | /rest/v1/accounts | - |
| 301 | /rest/v1/account | - |
| 301 | /rest/v1/me | - |
| 301 | /rest/v1/profile | - |
| 301 | /rest/v1/whoami | - |
| 301 | /rest/v1/auth | - |
| 301 | /rest/v1/login | - |
| 301 | /rest/v1/logout | - |
| 301 | /rest/v1/register | - |
| 301 | /rest/v1/signup | - |
| 301 | /rest/v1/signin | - |
| 301 | /rest/v1/token | - |
| 301 | /rest/v1/tokens | - |
| 301 | /rest/v1/refresh | - |
| 301 | /rest/v1/session | - |
| 301 | /rest/v1/sessions | - |
| 301 | /rest/v1/password | - |
| 301 | /rest/v1/reset-password | - |
| 301 | /rest/v1/forgot-password | - |
| 301 | /rest/v1/2fa | - |
| 301 | /rest/v1/mfa | - |
| 301 | /rest/v1/otp | - |
| 301 | /rest/v1/admin | - |
| 301 | /rest/v1/config | - |
| 301 | /rest/v1/settings | - |
| 301 | /rest/v1/flags | - |
| 301 | /rest/v1/feature-flags | - |
| 301 | /rest/v1/permissions | - |
| 301 | /rest/v1/roles | - |
| 301 | /rest/v1/groups | - |
| 301 | /rest/v1/privileges | - |
| 301 | /rest/v1/audit | - |
| 301 | /rest/v1/audit-log | - |
| 301 | /rest/v1/logs | - |
| 301 | /rest/v1/events | - |
| 301 | /rest/v1/data | - |
| 301 | /rest/v1/items | - |
| 301 | /rest/v1/products | - |
| 301 | /rest/v1/orders | - |
| 301 | /rest/v1/invoices | - |
| 301 | /rest/v1/payments | - |
| 301 | /rest/v1/transactions | - |
| 301 | /rest/v1/transfer | - |
| 301 | /rest/v1/transfers | - |
| 301 | /rest/v1/wallets | - |
| 301 | /rest/v1/balance | - |
| 301 | /rest/v1/subscriptions | - |
| 301 | /rest/v1/plans | - |
| 301 | /rest/v1/billing | - |
| 301 | /rest/v1/cart | - |
| 301 | /rest/v1/checkout | - |
| 301 | /rest/v1/notes | - |
| 301 | /rest/v1/messages | - |
| 301 | /rest/v1/comments | - |
| 301 | /rest/v1/chats | - |
| 301 | /rest/v1/posts | - |
| 301 | /rest/v1/articles | - |
| 301 | /rest/v1/files | - |
| 301 | /rest/v1/uploads | - |
| 301 | /rest/v1/documents | - |
| 301 | /rest/v1/attachments | - |
| 301 | /rest/v1/images | - |
| 301 | /rest/v1/search | - |
| 301 | /rest/v1/media | - |
| 301 | /rest/v1/filter | - |
| 301 | /rest/v1/query | - |
| 301 | /rest/v1/tags | - |
| 301 | /rest/v1/categories | - |
| 301 | /rest/v1/stats | - |
| 301 | /rest/v1/metrics | - |
| 301 | /rest/v1/health | - |
| 301 | /rest/v1/status | - |
| 301 | /rest/v1/version | - |
| 301 | /rest/v1/info | - |
| 301 | /rest/v1/debug | - |
| 301 | /rest/v1/test | - |
| 301 | /rest/v1/internal | - |
| 301 | /rest/v1/private | - |
| 301 | /rest/v1/hidden | - |
| 301 | /rest/v1/keys | - |
| 301 | /rest/v1/secrets | - |
| 301 | /rest/v1/credentials | - |
| 301 | /rest/v1/api-keys | - |
| 301 | /rest/v1/export | - |
| 301 | /rest/v1/import | - |
| 301 | /rest/v1/backup | - |
| 301 | /rest/v1/dump | - |
| 301 | /rest/v1/report | - |
| 301 | /rest/v1/reports | - |
| 301 | /rest/v1/notifications | - |
| 301 | /rest/v1/webhooks | - |
| 301 | /rest/v1/callbacks | - |
| 301 | /rest/v1/subscribe | - |
| 301 | /rest/v1/feed | - |
| 301 | /rest/v1/feeds | - |
| 301 | /rest/v1/activity | - |
| 301 | /rest/v1/history | - |
| 301 | /rest/v2/user | - |
| 301 | /rest/v2/accounts | - |
| 301 | /rest/v2/users | - |
| 301 | /rest/v2/account | - |
| 301 | /rest/v2/me | - |
| 301 | /rest/v2/profile | - |
| 301 | /rest/v2/whoami | - |
| 301 | /rest/v2/auth | - |
| 301 | /rest/v2/login | - |
| 301 | /rest/v2/register | - |
| 301 | /rest/v2/logout | - |
| 301 | /rest/v2/signup | - |
| 301 | /rest/v2/token | - |
| 301 | /rest/v2/tokens | - |
| 301 | /rest/v2/signin | - |
| 301 | /rest/v2/refresh | - |
| 301 | /rest/v2/session | - |
| 301 | /rest/v2/sessions | - |
| 301 | /rest/v2/password | - |
| 301 | /rest/v2/reset-password | - |
| 301 | /rest/v2/forgot-password | - |
| 301 | /rest/v2/mfa | - |
| 301 | /rest/v2/otp | - |
| 301 | /rest/v2/2fa | - |
| 301 | /rest/v2/admin | - |
| 301 | /rest/v2/config | - |
| 301 | /rest/v2/settings | - |
| 301 | /rest/v2/flags | - |
| 301 | /rest/v2/feature-flags | - |
| 301 | /rest/v2/permissions | - |
| 301 | /rest/v2/groups | - |
| 301 | /rest/v2/roles | - |
| 301 | /rest/v2/privileges | - |
| 301 | /rest/v2/audit | - |
| 301 | /rest/v2/audit-log | - |
| 301 | /rest/v2/logs | - |
| 301 | /rest/v2/events | - |
| 301 | /rest/v2/data | - |
| 301 | /rest/v2/items | - |
| 301 | /rest/v2/orders | - |
| 301 | /rest/v2/products | - |
| 301 | /rest/v2/invoices | - |
| 301 | /rest/v2/payments | - |
| 301 | /rest/v2/transactions | - |
| 301 | /rest/v2/transfer | - |
| 301 | /rest/v2/transfers | - |
| 301 | /rest/v2/wallets | - |
| 301 | /rest/v2/plans | - |
| 301 | /rest/v2/subscriptions | - |
| 301 | /rest/v2/balance | - |
| 301 | /rest/v2/cart | - |
| 301 | /rest/v2/checkout | - |
| 301 | /rest/v2/notes | - |
| 301 | /rest/v2/messages | - |
| 301 | /rest/v2/chats | - |
| 301 | /rest/v2/posts | - |
| 301 | /rest/v2/articles | - |
| 301 | /rest/v2/comments | - |
| 301 | /rest/v2/files | - |
| 301 | /rest/v2/documents | - |
| 301 | /rest/v2/uploads | - |
| 301 | /rest/v2/attachments | - |
| 301 | /rest/v2/media | - |
| 301 | /rest/v2/images | - |
| 301 | /rest/v2/search | - |
| 301 | /rest/v2/filter | - |
| 301 | /rest/v2/query | - |
| 301 | /rest/v2/tags | - |
| 301 | /rest/v2/categories | - |
| 301 | /rest/v2/stats | - |
| 301 | /rest/v2/health | - |
| 301 | /rest/v2/metrics | - |
| 301 | /rest/v2/billing | - |
| 301 | /rest/v2/status | - |
| 301 | /rest/v2/version | - |
| 301 | /rest/v2/info | - |
| 301 | /rest/v2/debug | - |
| 301 | /rest/v2/test | - |
| 301 | /rest/v2/private | - |
| 301 | /rest/v2/internal | - |
| 301 | /rest/v2/hidden | - |
| 301 | /rest/v2/secrets | - |
| 301 | /rest/v2/keys | - |
| 301 | /rest/v2/credentials | - |
| 301 | /rest/v2/api-keys | - |
| 301 | /rest/v2/export | - |
| 301 | /rest/v2/backup | - |
| 301 | /rest/v2/import | - |
| 301 | /rest/v2/dump | - |
| 301 | /rest/v2/report | - |
| 301 | /rest/v2/reports | - |
| 301 | /rest/v2/notifications | - |
| 301 | /rest/v2/webhooks | - |
| 301 | /rest/v2/callbacks | - |
| 301 | /rest/v2/feed | - |
| 301 | /rest/v2/subscribe | - |
| 301 | /rest/v2/activity | - |
| 301 | /rest/v2/feeds | - |
| 301 | /rest/v2/history | - |
| 301 | /services/users | - |
| 301 | /services/user | - |
| 301 | /services/accounts | - |
| 301 | /services/account | - |
| 301 | /services/me | - |
| 301 | /services/profile | - |
| 301 | /services/auth | - |
| 301 | /services/whoami | - |
| 301 | /services/login | - |
| 301 | /services/logout | - |
| 301 | /services/register | - |
| 301 | /services/signup | - |
| 301 | /services/signin | - |
| 301 | /services/token | - |
| 301 | /services/refresh | - |
| 301 | /services/tokens | - |
| 301 | /services/session | - |
| 301 | /services/sessions | - |
| 301 | /services/password | - |
| 301 | /services/forgot-password | - |
| 301 | /services/reset-password | - |
| 301 | /services/mfa | - |
| 301 | /services/2fa | - |
| 301 | /services/admin | - |
| 301 | /services/otp | - |
| 301 | /services/config | - |
| 301 | /services/settings | - |
| 301 | /services/flags | - |
| 301 | /services/feature-flags | - |
| 301 | /services/roles | - |
| 301 | /services/permissions | - |
| 301 | /services/groups | - |
| 301 | /services/privileges | - |
| 301 | /services/audit | - |
| 301 | /services/audit-log | - |
| 301 | /services/logs | - |
| 301 | /services/events | - |
| 301 | /services/data | - |
| 301 | /services/items | - |
| 301 | /services/products | - |
| 301 | /services/invoices | - |
| 301 | /services/orders | - |
| 301 | /services/payments | - |
| 301 | /services/transactions | - |
| 301 | /services/transfer | - |
| 301 | /services/transfers | - |
| 301 | /services/wallets | - |
| 301 | /services/balance | - |
| 301 | /services/subscriptions | - |
| 301 | /services/plans | - |
| 301 | /services/billing | - |
| 301 | /services/cart | - |
| 301 | /services/checkout | - |
| 301 | /services/notes | - |
| 301 | /services/messages | - |
| 301 | /services/chats | - |
| 301 | /services/comments | - |
| 301 | /services/articles | - |
| 301 | /services/posts | - |
| 301 | /services/files | - |
| 301 | /services/uploads | - |
| 301 | /services/attachments | - |
| 301 | /services/documents | - |
| 301 | /services/media | - |
| 301 | /services/images | - |
| 301 | /services/search | - |
| 301 | /services/query | - |
| 301 | /services/filter | - |
| 301 | /services/tags | - |
| 301 | /services/stats | - |
| 301 | /services/categories | - |
| 301 | /services/metrics | - |
| 301 | /services/health | - |
| 301 | /services/status | - |
| 301 | /services/version | - |
| 301 | /services/debug | - |
| 301 | /services/info | - |
| 301 | /services/test | - |
| 301 | /services/internal | - |
| 301 | /services/hidden | - |
| 301 | /services/credentials | - |
| 301 | /services/secrets | - |
| 301 | /services/private | - |
| 301 | /services/keys | - |
| 301 | /services/api-keys | - |
| 301 | /services/export | - |
| 301 | /services/backup | - |
| 301 | /services/import | - |
| 301 | /services/dump | - |
| 301 | /services/report | - |
| 301 | /services/notifications | - |
| 301 | /services/reports | - |
| 301 | /services/webhooks | - |
| 301 | /services/callbacks | - |
| 301 | /services/subscribe | - |
| 301 | /services/feed | - |
| 301 | /services/feeds | - |
| 301 | /services/api/users | - |
| 301 | /services/activity | - |
| 301 | /services/api/user | - |
| 301 | /services/history | - |
| 301 | /services/api/accounts | - |
| 301 | /services/api/account | - |
| 301 | /services/api/me | - |
| 301 | /services/api/profile | - |
| 301 | /services/api/whoami | - |
| 301 | /services/api/auth | - |
| 301 | /services/api/login | - |
| 301 | /services/api/logout | - |
| 301 | /services/api/register | - |
| 301 | /services/api/signup | - |
| 301 | /services/api/signin | - |
| 301 | /services/api/token | - |
| 301 | /services/api/tokens | - |
| 301 | /services/api/refresh | - |
| 301 | /services/api/sessions | - |
| 301 | /services/api/session | - |
| 301 | /services/api/password | - |
| 301 | /services/api/forgot-password | - |
| 301 | /services/api/reset-password | - |
| 301 | /services/api/2fa | - |
| 301 | /services/api/mfa | - |
| 301 | /services/api/otp | - |
| 301 | /services/api/admin | - |
| 301 | /services/api/config | - |
| 301 | /services/api/settings | - |
| 301 | /services/api/feature-flags | - |
| 301 | /services/api/flags | - |
| 301 | /services/api/permissions | - |
| 301 | /services/api/roles | - |
| 301 | /services/api/groups | - |
| 301 | /services/api/privileges | - |
| 301 | /services/api/audit | - |
| 301 | /services/api/audit-log | - |
| 301 | /services/api/logs | - |
| 301 | /services/api/events | - |
| 301 | /services/api/data | - |
| 301 | /services/api/items | - |
| 301 | /services/api/products | - |
| 301 | /services/api/invoices | - |
| 301 | /services/api/orders | - |
| 301 | /services/api/payments | - |
| 301 | /services/api/transfer | - |
| 301 | /services/api/transfers | - |
| 301 | /services/api/transactions | - |
| 301 | /services/api/wallets | - |
| 301 | /services/api/balance | - |
| 301 | /services/api/subscriptions | - |
| 301 | /services/api/plans | - |
| 301 | /services/api/billing | - |
| 301 | /services/api/cart | - |
| 301 | /services/api/checkout | - |
| 301 | /services/api/notes | - |
| 301 | /services/api/messages | - |
| 301 | /services/api/chats | - |
| 301 | /services/api/posts | - |
| 301 | /services/api/comments | - |
| 301 | /services/api/articles | - |
| 301 | /services/api/files | - |
| 301 | /services/api/uploads | - |
| 301 | /services/api/documents | - |
| 301 | /services/api/attachments | - |
| 301 | /services/api/images | - |
| 301 | /services/api/search | - |
| 301 | /services/api/media | - |
| 301 | /services/api/query | - |
| 301 | /services/api/filter | - |
| 301 | /services/api/tags | - |
| 301 | /services/api/categories | - |
| 301 | /services/api/stats | - |
| 301 | /services/api/metrics | - |
| 301 | /services/api/health | - |
| 301 | /services/api/status | - |
| 301 | /services/api/version | - |
| 301 | /services/api/debug | - |
| 301 | /services/api/info | - |
| 301 | /services/api/test | - |
| 301 | /services/api/internal | - |
| 301 | /services/api/hidden | - |
| 301 | /services/api/private | - |
| 301 | /services/api/keys | - |
| 301 | /services/api/secrets | - |
| 301 | /services/api/api-keys | - |
| 301 | /services/api/credentials | - |
| 301 | /services/api/export | - |
| 301 | /services/api/import | - |
| 301 | /services/api/backup | - |
| 301 | /services/api/report | - |
| 301 | /services/api/dump | - |
| 301 | /services/api/reports | - |
| 301 | /services/api/webhooks | - |
| 301 | /services/api/callbacks | - |
| 301 | /services/api/notifications | - |
| 301 | /services/api/subscribe | - |
| 301 | /services/api/feed | - |
| 301 | /services/api/activity | - |
| 301 | /services/api/feeds | - |
| 301 | /services/api/history | - |
| 301 | /actuator/accounts | - |
| 301 | /actuator/users | - |
| 301 | /actuator/account | - |
| 301 | /actuator/user | - |
| 301 | /actuator/me | - |
| 301 | /actuator/profile | - |
| 301 | /actuator/whoami | - |
| 301 | /actuator/auth | - |
| 301 | /actuator/signup | - |
| 301 | /actuator/register | - |
| 301 | /actuator/login | - |
| 301 | /actuator/logout | - |
| 301 | /actuator/signin | - |
| 301 | /actuator/token | - |
| 301 | /actuator/tokens | - |
| 301 | /actuator/refresh | - |
| 301 | /actuator/sessions | - |
| 301 | /actuator/session | - |
| 301 | /actuator/password | - |
| 301 | /actuator/forgot-password | - |
| 301 | /actuator/reset-password | - |
| 301 | /actuator/2fa | - |
| 301 | /actuator/mfa | - |
| 301 | /actuator/otp | - |
| 301 | /actuator/admin | - |
| 301 | /actuator/config | - |
| 301 | /actuator/settings | - |
| 301 | /actuator/flags | - |
| 301 | /actuator/feature-flags | - |
| 301 | /actuator/permissions | - |
| 301 | /actuator/roles | - |
| 301 | /actuator/groups | - |
| 301 | /actuator/audit-log | - |
| 301 | /actuator/audit | - |
| 301 | /actuator/logs | - |
| 301 | /actuator/privileges | - |
| 301 | /actuator/data | - |
| 301 | /actuator/events | - |
| 301 | /actuator/products | - |
| 301 | /actuator/items | - |
| 301 | /actuator/payments | - |
| 301 | /actuator/invoices | - |
| 301 | /actuator/orders | - |
| 301 | /actuator/transactions | - |
| 301 | /actuator/transfer | - |
| 301 | /actuator/transfers | - |
| 301 | /actuator/wallets | - |
| 301 | /actuator/balance | - |
| 301 | /actuator/subscriptions | - |
| 301 | /actuator/plans | - |
| 301 | /actuator/billing | - |
| 301 | /actuator/cart | - |
| 301 | /actuator/checkout | - |
| 301 | /actuator/notes | - |
| 301 | /actuator/messages | - |
| 301 | /actuator/chats | - |
| 301 | /actuator/comments | - |
| 301 | /actuator/posts | - |
| 301 | /actuator/articles | - |
| 301 | /actuator/files | - |
| 301 | /actuator/uploads | - |
| 301 | /actuator/attachments | - |
| 301 | /actuator/documents | - |
| 301 | /actuator/media | - |
| 301 | /actuator/images | - |
| 301 | /actuator/filter | - |
| 301 | /actuator/search | - |
| 301 | /actuator/query | - |
| 301 | /actuator/tags | - |
| 301 | /actuator/categories | - |
| 301 | /actuator/stats | - |
| 301 | /actuator/metrics | - |
| 301 | /actuator/status | - |
| 301 | /actuator/info | - |
| 301 | /actuator/version | - |
| 301 | /actuator/test | - |
| 301 | /actuator/debug | - |
| 301 | /actuator/internal | - |
| 301 | /actuator/hidden | - |
| 301 | /actuator/private | - |
| 301 | /actuator/keys | - |
| 301 | /actuator/secrets | - |
| 301 | /actuator/credentials | - |
| 301 | /actuator/api-keys | - |
| 301 | /actuator/export | - |
| 301 | /actuator/import | - |
| 301 | /actuator/backup | - |
| 301 | /actuator/dump | - |
| 301 | /actuator/report | - |
| 301 | /actuator/reports | - |
| 301 | /actuator/notifications | - |
| 301 | /actuator/webhooks | - |
| 301 | /actuator/callbacks | - |
| 301 | /actuator/subscribe | - |
| 301 | /actuator/feed | - |
| 301 | /actuator/feeds | - |
| 301 | /actuator/activity | - |
| 301 | /actuator/history | - |

## Pruebas Avanzadas de Seguridad

| Modulo | Hallazgos |
| --- | --- |
| SSRF | 0 |
| SSTI | 0 |
| XXE | 0 |
| CRLF | 24 |
| HTTP Request Smuggling | 0 |
| Cache Poisoning | 1 |

### CRLF Injection (24)

| Vector | URL/Param | Payload |
| --- | --- | --- |
| path | http://app.notap.io/%0d%0aSet-Cookie%3Acrlf_test%3Dinjected | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | url | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | redirect | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | next | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | return | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | r | %0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| path | http://app.notap.io/%0aSet-Cookie%3Acrlf_test%3Dinjected | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | url | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | redirect | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | next | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | return | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | r | %0aSet-Cookie%3Acrlf_test%3Dinjected |
| path | http://app.notap.io//%0d%0aSet-Cookie%3Acrlf_test%3Dinjected | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | url | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | redirect | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | next | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | return | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| param | r | /%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| path | http://app.notap.io/%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |
| param | url | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |
| param | redirect | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |
| param | next | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |
| param | return | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |
| param | r | %E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjecte |

### Cache Poisoning (1)

| Cabecera | Valor inyectado | Estado |
| --- | --- | --- |
| X-Forwarded-Scheme | http | Confirmado |

## Pruebas de inyección

| Métrica | Valor |
| --- | --- |
| Formularios detectados | 0 |
| Parámetros GET detectados | 0 |
| Parámetros GET probados | 0 |
| Inputs de formulario probados | 0 |

## Vulnerabilidades por severidad (Nuclei)

| Severidad | Cantidad | Templates únicos |
| --- | --- | --- |
| INFO | 3 | caa-fingerprint, dns-saas-service-detection, http-missing-security-headers |

## Hallazgos clasificados (total: 68)

| Categoría | Cantidad |
| --- | --- |
| NMAP:NSE | 9 |
| NUCLEI:INFO | 3 |
| PORT | 2 |
| VULN | 54 |

### Detalle de hallazgos (68)

| Categoría | Detalle |
| --- | --- |
| VULN | Sin rate limiting detectado en http://app.notap.io/api/v1/login: 25 peticiones sin bloqueo |
| VULN | Sin rate limiting: Sin rate limiting detectado en http://app.notap.io/api/v1/login: 25 peticiones sin bloqueo |
| PORT | 69.46.46.120:80/tcp http (Pingora) |
| PORT | 69.46.46.120:443/tcp https (Pingora) |
| NMAP:NSE | 69.46.46.120:80/tcp http-stored-xss - Couldn't find any stored XSS vulnerabilities. |
| NMAP:NSE | 69.46.46.120:80/tcp http-dombased-xss - Couldn't find any DOM based XSS. |
| NMAP:NSE | 69.46.46.120:80/tcp http-xssed - No previously reported XSS vuln. |
| NMAP:NSE | 69.46.46.120:80/tcp http-csrf - Couldn't find any CSRF vulnerabilities. |
| NMAP:NSE | 69.46.46.120:80/tcp http-slowloris-check - VULNERABLE: |
| NMAP:NSE | 69.46.46.120:443/tcp http-dombased-xss - Couldn't find any DOM based XSS. |
| NMAP:NSE | 69.46.46.120:443/tcp http-stored-xss - Couldn't find any stored XSS vulnerabilities. |
| NMAP:NSE | 69.46.46.120:443/tcp http-csrf - Couldn't find any CSRF vulnerabilities. |
| NMAP:NSE | 69.46.46.120:443/tcp http-xssed - No previously reported XSS vuln. |
| NUCLEI:INFO | caa-fingerprint — CAA Record @ app.notap.io |
| NUCLEI:INFO | dns-saas-service-detection — DNS SaaS Service Detection @ app.notap.io |
| NUCLEI:INFO | http-missing-security-headers — HTTP Missing Security Headers @ https://app.notap.io/ |
| VULN | CRLF confirmado en path: http://app.notap.io/%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado en path: http://app.notap.io/%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinject |
| VULN | CRLF Injection: CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinject |
| VULN | CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Di |
| VULN | CRLF Injection: CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Di |
| VULN | CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinjec |
| VULN | CRLF Injection: CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinjec |
| VULN | CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinj |
| VULN | CRLF Injection: CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinj |
| VULN | CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado en path: http://app.notap.io/%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado en path: http://app.notap.io/%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinje |
| VULN | CRLF Injection: CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinje |
| VULN | CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinject |
| VULN | CRLF Injection: CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinject |
| VULN | CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado en path: http://app.notap.io//%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado en path: http://app.notap.io//%0d%0aSet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinjec |
| VULN | CRLF Injection: CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinjec |
| VULN | CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3D |
| VULN | CRLF Injection: CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3D |
| VULN | CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinje |
| VULN | CRLF Injection: CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinje |
| VULN | CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Din |
| VULN | CRLF Injection: CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Din |
| VULN | CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinjecte |
| VULN | CRLF Injection: CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com/%0d%0aSet-Cookie%3Acrlf_test%3Dinjecte |
| VULN | CRLF confirmado en path: http://app.notap.io/%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF Injection: CRLF confirmado en path: http://app.notap.io/%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_test%3Dinjected |
| VULN | CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_t |
| VULN | CRLF Injection: CRLF confirmado via parametro 'url': http://app.notap.io?url=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_t |
| VULN | CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Ac |
| VULN | CRLF Injection: CRLF confirmado via parametro 'redirect': http://app.notap.io?redirect=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Ac |
| VULN | CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_ |
| VULN | CRLF Injection: CRLF confirmado via parametro 'next': http://app.notap.io?next=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_ |
| VULN | CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrl |
| VULN | CRLF Injection: CRLF confirmado via parametro 'return': http://app.notap.io?return=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrl |
| VULN | CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_tes |
| VULN | CRLF Injection: CRLF confirmado via parametro 'r': http://app.notap.io?r=https://example.com%E5%98%8D%E5%98%8ASet-Cookie%3Acrlf_tes |
| VULN | Cache Poisoning confirmado via 'X-Forwarded-Scheme: http' — valor inyectado persiste en respuesta sin cabecera |
| VULN | Cache Poisoning: Cache Poisoning confirmado via 'X-Forwarded-Scheme: http' — valor inyectado persiste en respuesta sin cabecera |
| VULN | Sin rate limiting detectado en http://app.notap.io/api/v1/login: 15 peticiones sin bloqueo |
| VULN | Sin rate limiting: Sin rate limiting detectado en http://app.notap.io/api/v1/login: 15 peticiones sin bloqueo |

---

_Generado automáticamente por WSTG Scanner._