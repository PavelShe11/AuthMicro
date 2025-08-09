
## Работа с авторизацией

### Запуск проекта
```
docker compose up --build
```

### URL и JSON данные
#### http://localhost:8080/auth/v1/registration

input:
```
{
    "email": "test@mail.ru"
}
```

output:
```
{
    "registrationId": "203551b1-d6fb-4c8c-8507-4e20e42fe88e",
    "codeExpires": "2025-07-24T14:39:35.779333Z",
    "code": "510233"
}
```

#### http://localhost:8080/auth/v1/registration/confirmEmail

input:
```
{
    "registrationId": "203551b1-d6fb-4c8c-8507-4e20e42fe88e",
    "email": "test@mail.ru",
    "code": "510233"
}
```

output:
```
{
    OK 200 status
}
```

#### http://localhost:8080/auth/v1/login/confirmEmail

input:
```
{
    "email": "test@mail.ru",
    "code": "510233"
}
```

output:
```
{
    "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwiaXNzIjoibWljcm8tYXV0aCIsImlhdCI6MTc1MzM2NzkxMywiZXhwIjoxNzUzMzY4MjEzfQ.cfEY1YxFdvJr8hQ8yY0qD5L9nCpELI4bWqMGBYNxhlPBCH55zKoW6Pp5EhYDnZJO5BUDgPu00PdG_pEpIGWaEQ",
    "requestToken": "eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoicmVmcmVzaCIsImlzcyI6Im1pY3JvLWF1dGgiLCJpYXQiOjE3NTMzNjc5MTMsImV4cCI6MTc1Mzc5OTkxM30.I58NRlcAg0WiSOKTbV7s73AgRT9kGLDUt62GbJHGU9mkzn1BW7PwXjmxEHSn-sSE829Cuxf-woyLSx75O2rHQw"
}
```


