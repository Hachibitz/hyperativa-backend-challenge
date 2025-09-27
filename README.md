# **Desafio Backend Hyperativa**

Esta é uma implementação para o desafio de backend da Hyperativa, construída com Java 21, Spring Boot 3 e uma arquitetura de microsserviços orientada a eventos.

A solução é composta por dois serviços principais (api e card-consumer) e utiliza um message broker (RabbitMQ) para comunicação assíncrona, com todas as configurações e dados sensíveis gerenciados de forma segura.

## **Arquitetura do fluxo de processamento (foco no processamento de arquivo)**

<img width="1095" height="802" alt="image" src="https://github.com/user-attachments/assets/d67bcb22-f584-421f-b7b3-139ad38250d2" />

## **🚀 Rodando o Projeto Localmente com Docker**

Com este setup, você pode rodar toda a stack da aplicação (API, Consumer, Banco de Dados e Fila) com um único comando.

### **Pré-requisitos**

- [Docker](https://www.docker.com/products/docker-desktop/) instalado e em execução.
- Docker Compose (geralmente já vem com o Docker Desktop).

### **Instruções para Execução**

- Clone este repositório para a sua máquina.
- Abra um terminal na pasta raiz do projeto (onde o arquivo docker-compose.yml está localizado).

Execute o seguinte comando para construir as imagens e iniciar todos os containers:  
```bash
docker-compose up --build
```

- - O --build é importante na primeira vez para construir as imagens Java. Nas próximas, você pode usar apenas docker-compose up.
    - **Observação:** É possível que, na primeira execução, os serviços da aplicação iniciem antes do banco de dados estar totalmente pronto, causando um erro. Se isso acontecer, sem problemas: pressione Ctrl + C para parar os contêineres e rode docker-compose up (sem o parâmetro --build) novamente.

### **Acessando os Serviços**

- **API**: A API estará disponível em <https://localhost:8080/api>.
  - A documentação Swagger/OpenAPI pode ser acessada em **<https://localhost:8080/api/swagger-ui.html>**.
  - Vai ter um aviso de conexão não segura pq o certificado é auto assinado apenas para o desafio, só continuar e ignorar esse aviso.
- **RabbitMQ Management**: A interface web do RabbitMQ estará em <http://localhost:15672>.
  - **Usuário**: hyper_user
  - **Senha**: hyper_password

### **Encerrando a Aplicação**

Para parar e remover todos os containers, pressione Ctrl + C no terminal onde o compose está rodando e depois execute:

```bash
docker-compose down
```

## **✅ Como Testar a API (Passo a Passo)**

Para interagir com os endpoints protegidos, siga os passos de autenticação abaixo.

### **Setup de Autenticação (Necessário para ambos os fluxos)**

- **Acesse a documentação do Swagger**: [https://localhost:8080/api/swagger-ui.html](https://localhost:8080/api/swagger-ui.html)
- **Obtenha o Token de Acesso**: Vá até o endpoint POST /auth/login e execute-o com as seguintes credenciais para obter um token JWT:
  - **Usuário**: testuser
  - **Senha**: password
- **Autorize suas Requisições**: Copie o token JWT retornado. No topo direito da página do Swagger, clique no botão **"Authorize"**, cole o token no formato &lt;seu-token-jwt&gt; (sem o prefixo Bearer, apenas o token puro) e clique em "Authorize". A partir de agora, todas as suas requisições via Swagger estarão autenticadas.

### **Fluxo 1: Inserindo um Cartão Individualmente (E2EE)**

Este fluxo demonstra a criptografia ponta a ponta.

- **Pegue a Chave Pública**: Execute o endpoint GET /security/public-key. Ele retornará a chave pública RSA da API em formato Base64.
- **Criptografe o Cartão**: Vá ao endpoint POST /security/encrypt-card. Cole a chave pública obtida no passo anterior no campo publicKey e insira um número de cartão no campo cardNumber. Execute para obter a versão criptografada do número.
- **Insira o Cartão Criptografado**: Execute o endpoint POST /cards, passando no corpo da requisição o valor do encryptedCardNumber retornado no passo 2 (se não for um cartão valido, precisa passar o parâmetro isToUseLuhnAlg como false, ou não vai inserir).
- **Verifique a Existência**: Execute o endpoint GET /cards/check, passando o mesmo valor criptografado como parâmetro (encryptedCard). O resultado esperado é o ID único do cartão no sistema, confirmando que ele foi salvo e encontrado com sucesso.

### **Fluxo 2: Processando um Arquivo de Lote (.txt)**

Este fluxo demonstra o processamento assíncrono de arquivos.

- **Siga o Setup de Autenticação** acima (passos 1 a 3).
- **Envie o Arquivo**: Vá até o endpoint POST /cards/upload.
  - No parâmetro isToUseLuhnAlg, especifique true (padrão) para validar os cartões com o algoritmo de Luhn ou false para pular essa validação.
  - Clique em "Choose File" e selecione o arquivo DESAFIO-HYPERATIVA.txt (ou outro de sua preferência) da sua máquina.
  - Execute a requisição. O retorno será um jobId com status 202 ACCEPTED.
- **Acompanhe o Processamento**: Execute o endpoint GET /batches/{jobId}, substituindo {jobId} pelo ID retornado no passo anterior. Você poderá ver o status do processamento do lote (PROCESSING, COMPLETED, COMPLETED_WITH_ERRORS, etc.).

## **✨ Funcionalidades e Requisitos Atendidos**

Para atender aos requisitos do desafio, diversas técnicas e padrões de mercado foram implementados, focando em segurança, resiliência e boas práticas de desenvolvimento.

- 🔐 **Segurança e Criptografia de Ponta a Ponta (End-to-End Encryption):**
  - **Em Trânsito**: A comunicação com a API é protegida com **HTTPS/TLS**, garantindo que os dados não possam ser interceptados entre o cliente e o servidor.
  - **No Payload**: Para operações sensíveis (inserção e consulta de cartões), a API expõe uma chave pública **RSA** para que o cliente possa criptografar os dados antes de enviá-los.
  - **Em Repouso**: Os números de cartão são armazenados no banco de dados utilizando criptografia simétrica **AES-256**, garantindo que mesmo em caso de acesso direto ao banco, os dados permaneçam seguros.
- ⚡ **Performance e Segurança em Consultas:**
  - Em vez de consultar diretamente pelo número do cartão (mesmo que criptografado), a solução armazena um **Hash (SHA-256)** do número. As buscas são feitas por este hash, que é **indexado** no banco de dados, resultando em consultas extremamente rápidas e seguras, sem expor o dado original.
- 📦 **Confiabilidade com Transactional Outbox:**
  - Para garantir que nenhuma mensagem de cartão seja perdida caso o RabbitMQ esteja indisponível, foi implementado o padrão **Transactional Outbox**. As mensagens são salvas na mesma transação da operação de negócio em uma tabela "outbox". Um job agendado é responsável por ler essa tabela e enviar as mensagens para a fila de forma confiável.
- 💪 **Resiliência no Consumidor:**
  - O serviço card-consumer foi construído para ser resiliente a falhas. Ele utiliza uma política de **Retentativas (Retry) com Exponential Backoff**, tentando reprocessar mensagens que falharam em intervalos de tempo crescentes.
  - Após esgotar as tentativas, a mensagem é movida para uma **Dead Letter Queue (DLQ)**, evitando que uma única mensagem com problema trave todo o processamento e permitindo análise posterior da falha.
- 📝 **Observabilidade e Boas Práticas:**
  - **Logging Detalhado**: Foi criado um Aspecto (@Loggable) que intercepta as chamadas dos endpoints e gera logs automáticos de entrada e saída, facilitando o rastreamento de requisições.
  - **Mascaramento de Dados Sensíveis**: Um utilitário de mascaramento (MaskingUtil) garante que dados como cardNumber, password e token nunca apareçam em texto plano nos logs da aplicação.
  - **Processamento de Arquivos Resiliente**: A lógica de upload de arquivos em lote é capaz de validar cada cartão individualmente. Cartões inválidos são registrados como falha, mas **não interrompem** o processamento dos cartões válidos no mesmo arquivo.
- 🏛️ **Arquitetura Modular e Consistente:**
  - A solução foi modularizada com uma biblioteca compartilhada (card-common) para centralizar entidades, repositórios e configurações comuns (como a do RabbitMQ), evitando duplicação de código e garantindo consistência entre os microsserviços.
