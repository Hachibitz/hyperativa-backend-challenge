# Desafio Backend Hyperativa

Esta é uma implementação para o desafio de backend da Hyperativa, construída com Java, Spring Boot e uma arquitetura de microsserviços orientada a eventos.

A solução é composta por dois serviços principais (`api` e `card-consumer`) e utiliza um message broker (RabbitMQ) para comunicação assíncrona, com todas as configurações e dados sensíveis gerenciados de forma segura.

## 🚀 Rodando o Projeto Localmente com Docker

Com este setup, você pode rodar toda a stack da aplicação (API, Consumer, Banco de Dados e Fila) com um único comando.

### Pré-requisitos

* [Docker](https://www.docker.com/products/docker-desktop/) instalado e em execução.
* Docker Compose (geralmente já vem com o Docker Desktop).

### Instruções para Execução

1.  Clone este repositório para a sua máquina.

2.  Abra um terminal na pasta raiz do projeto (onde o arquivo `docker-compose.yml` está localizado).

3.  Execute o seguinte comando para construir as imagens e iniciar todos os containers:

    ```bash
    docker-compose up --build
    ```
    * O `--build` é importante na primeira vez para construir as imagens Java. Nas próximas, você pode usar apenas `docker-compose up`.

4.  Aguarde até que todos os serviços estejam de pé e prontos. Você verá os logs de todos os containers no seu terminal.

### Acessando os Serviços

* **API**: A API estará disponível em `http://localhost:8080`.
    * A documentação Swagger/OpenAPI pode ser acessada em `http://localhost:8080/swagger-ui.html`.
* **RabbitMQ Management**: A interface web do RabbitMQ estará em `http://localhost:15672`.
    * **Usuário**: `hyper_user`
    * **Senha**: `hyper_password`

### Credenciais de Teste

Um usuário de teste com permissão `ROLE_USER` já foi criado automaticamente para você:
* **Usuário**: `testuser`
* **Senha**: `password`

### Encerrando a Aplicação

Para parar e remover todos os containers, pressione `Ctrl + C` no terminal onde o compose está rodando e depois execute:

```bash
docker-compose down
```
