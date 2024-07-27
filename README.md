# ![DevSuperior logo](https://raw.githubusercontent.com/devsuperior/bds-assets/main/ds/devsuperior-logo-small.png) DevSuperior: DSCommerce
>  *Este Projeto foi desenvolvido durante o Curso Spring Professional da DevSuperior*

![Java](https://img.shields.io/badge/java-FF5722.svg?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Hibernate](https://img.shields.io/badge/Hibernate-F57F17?style=for-the-badge&logo=Hibernate&logoColor=white)
![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white)
![PhpMyAdmin](https://img.shields.io/badge/PhpMyAdmin-6f42c1?style=for-the-badge&logo=phpmyadmin&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![OAuth2](https://img.shields.io/badge/OAuth2-F80000?style=for-the-badge&logo=openid&logoColor=white)

## Modelo de Domínio
![DiagramaDeClassesDevSuperior](https://github.com/user-attachments/assets/15eb52d3-0031-4746-9c77-72c9348369da)

### Serviço RESTful 🚀

* Desenvolvimento de um serviço RESTful para toda a aplicação.

## Tecnologias 💻
 
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring MVC](https://docs.spring.io/spring-framework/reference/web/webmvc.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [JWT](https://jwt.io/)
- [OAuth2](https://oauth.net/2/)
- [SpringDoc OpenAPI 3](https://springdoc.org/v2/#spring-webflux-support)
- [H2](https://www.baeldung.com/spring-boot-h2-database)
- [MySQL](https://www.mysql.com/)
- [PhpMyAdmin](https://www.phpmyadmin.net/)
- [Docker](https://www.docker.com/)
- [JUnit5](https://junit.org/junit5/)
- [Mockito](https://site.mockito.org/)
- [MockMvc](https://docs.spring.io/spring-framework/reference/testing/spring-mvc-test-framework.html)
- [RestAssured](https://rest-assured.io/)
- [Jacoco](https://www.eclemma.org/jacoco/)
- [Bean Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
- [HATEOAS](https://spring.io/projects/spring-hateoas)

## Competências Desenvolvidas durante o Curso

- Componentes e Injeção de Dependência
- Estruturação de projeto, camadas
- Modelo de Domínio
- Relacionamentos
- Mapeamento objeto relacional
- API Rest
- Tratamento de exceções
- Consultas ao banco de dados
- Transações
- Variáveis de ambiente
- Perfis de projeto
- Ambiente local, homologação
- Implantação, CI/CD
- CRUD, validação de dados, exceções
- Modelo de domínio, ORM
- Login e controle de acesso, OAuth2, JWT
- Testes automatizados
- TDD - Test Driven Development
- Testes de unidade
- Mock
- Spy
- Testes de repository
- Testes de camada service
- Testes de camada web, API
- Testes de integração
- Validação customizada
- Test coverage

## Como executar 🎉

1.Clonar repositório git:

```text
git clone https://github.com/FernandoCanabarroAhnert/devsuperior-backend-dscommerce.git
```

2.Instalar dependências.

```text
mvn clean install
```

3.Executar a aplicação Spring Boot.

4.Testar endpoints através do Postman ou da url
<http://localhost:8080/swagger-ui/index.html#/>

### Usando Docker 🐳

- Clonar repositório git
- Construir o projeto:
```
./mvnw clean package
```
- Construir a imagem:
```
./mvnw spring-boot:build-image
```
- Executar o container:
```
docker run --name dscommerce -p 8080:8080  -d dscommerce:0.0.1-SNAPSHOT
