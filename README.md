Projeto: Arquitetura de Microsserviços: Padrão Saga Coreografado

### Sumário:
- Tecnologias
- Ferramentas utilizadas
- Arquitetura Proposta
- Execução do projeto
    - Execução geral via docker-compose
    - Execução geral via automação com script em Python
    - Executando os serviços de bancos de dados e Message Broker
    - Executando manualmente via CLI
- Acessando a aplicação
### Acessando tópicos com Redpanda Console
- Dados da API
    - Produtos registrados e seu estoque
    - Endpoint para iniciar a saga
    - Endpoint para visualizar a saga
    - Acesso ao MongoDB

### Tecnologias
    - Java 17
    - Spring Boot 3
    - Apache Kafka
    - API REST
    - PostgreSQL
    - MongoDB
    - Docker
    - docker-compose
    - Redpanda Console

### Ferramentas utilizadas    
    - IntelliJ IDEA Community Edition
    - Docker
    - Gradle
### Arquitetura Proposta



Em nossa arquitetura, teremos 4 serviços:

- Order-Service: microsserviço responsável apenas por gerar um pedido inicial, e receber uma notificação. Aqui que teremos endpoints REST para inciar o processo e recuperar os dados dos eventos. O banco de dados utilizado será o MongoDB.
- Product-Validation-Service: microsserviço responsável por validar se o produto informado no pedido existe e está válido. Este microsserviço guardará a validação de um produto para o ID de um pedido. O banco de dados utilizado será o PostgreSQL.
- Payment-Service: microsserviço responsável por realizar um pagamento com base nos valores unitários e quantidades informadas no pedido. Este microsserviço guardará a informação de pagamento de um pedido. O banco de dados utilizado será o PostgreSQL.    
- Inventory-Service: microsserviço responsável por realizar a baixa do estoque dos produtos de um pedido. Este microsserviço guardará a informação da baixa de um produto para o ID de um pedido. O banco de dados utilizado será o PostgreSQL.

Todos os serviços da arquitetura irão subir através do arquivo docker-compose.yml.

### Execução do projeto
Há várias maneiras de executar os projetos:
- Executando tudo via docker-compose    Executando tudo via script de automação que eu disponibilizei (build.py)
- Executando apenas os serviços de bancos de dados e message broker (Kafka) separadamente
- Executando as aplicações manualmente via CLI (java -jar ou gradle bootRun ou via IntelliJ)

Para rodar as aplicações, será necessário ter instalado:
- Docker
- Java 17
- Gradle 7.6 ou superior

#### Execução geral via docker-compose
Basta executar o comando no diretório raiz do repositório:
```bash
  docker-compose up --build -d
```
Obs.: para rodar tudo desta maneira, é necessário realizar o build das 5 aplicações, veja nos passos abaixo sobre como fazer isto.

#### Execução geral via automação com script em Python
Basta executar o arquivo ```bash build.py ```. Para isto, é necessário ter o Python 3 instalado.

Para executar, basta apenas executar o seguinte comando no diretório raiz do repositório:
```bash 
python build.py 
```
#### Executando os serviços de bancos de dados e Message Broker
Para que seja possível executar os serviços de bancos de dados e Message Broker, como MongoDB, PostgreSQL e Apache Kafka, basta ir no diretório raiz do repositório, onde encontra-se o arquivo ```bash docker-compose.yml``` 
```bash 
docker-compose up --build -d order-db kafka product-db payment-db inventory-db
```
Como queremos rodar apenas os serviços de bancos de dados e Message Broker, é necessário informá-los no comando do ```bash docker-compose``` , caso contrário, as aplicações irão subir também.

Para parar todos os containers, basta rodar: ```bash docker-compose down```

Ou então: 
``` bash docker stop ($docker ps -aq) ``` | ```bash docker container prune -f```

#### Executando manualmente via CLI
Antes da execução do projeto, realize o ```build``` da aplicação indo no diretório raiz e executando o comando:
```bash 
gradle build -x test
``` 
Para executar os projetos com Gradle, basta entrar no diretório raiz de cada projeto, e executar o comando:
```bash 
gradle bootRun
``` 
Ou então, entrar no diretório: ```build/libs``` e executar o comando:
```bash
java -jar nome_do_jar.jar
```
### Acessando a aplicação
Para acessar as aplicações e realizar um pedido, basta acessar a URL:
http://localhost:3000/swagger-ui.html

Você chegará nesta página:



As aplicações executarão nas seguintes portas:
- Order-Service: 3000
- Product-Validation-Service: 8090
- Payment-Service: 8091
- Inventory-Service: 8092
- Apache Kafka: 9092
- Redpanda Console: 8081
- PostgreSQL (Product-DB): 5432
- PostgreSQL (Payment-DB): 5433
- PostgreSQL (Inventory-DB): 5434
- MongoDB (Order-DB): 27017

### Acessando tópicos com Redpanda Console

Para acessar o Redpanda Console e visualizar tópicos e publicar eventos, basta acessar: http://localhost:8081

Você chegará nesta página:



### Dados da API
É necessário conhecer o payload de envio ao fluxo da saga, assim como os produtos cadastrados e suas quantidades.

#### Produtos registrados e seu estoque
Existem 3 produtos iniciais cadastrados no serviço product-validation-service e suas quantidades disponíveis em ```inventory-service```:

- COMIC_BOOKS (4 em estoque)
- BOOKS (2 em estoque)
- MOVIES (5 em estoque)
- MUSIC (9 em estoque)
#### Endpoint para iniciar a saga: 
POST http://localhost:3000/api/order

Payload:
```
{
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.50
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.90
      },
      "quantity": 1
    }
  ]
}
``` 
Response:
``` {
  "id": "64429e987a8b646915b3735f",
  "products": [
    {
      "product": {
        "code": "COMIC_BOOKS",
        "unitValue": 15.5
      },
      "quantity": 3
    },
    {
      "product": {
        "code": "BOOKS",
        "unitValue": 9.9
      },
      "quantity": 1
    }
  ],
  "createdAt": "2023-04-21T14:32:56.335943085",
  "transactionId": "1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519"
} 
``` 

#### Endpoint para visualizar a saga:
É possível recuperar os dados da saga pelo orderId ou pelo transactionId, o resultado será o mesmo:

GET http://localhost:3000/api/event?orderId=64429e987a8b646915b3735f

GET http://localhost:3000/api/event?transactionId=1682087576536_99d2ca6c-f074-41a6-92e0-21700148b519

Response:
```
{
  "id": "6619c82a8fc7a05c03b0a31d",
  "transactionId": "1712965674008_ed396176-f12a-4afc-ac54-31a4a2519f78",
  "orderId": "6619c82a8fc7a05c03b0a31c",
  "payload": {
    "id": "6619c82a8fc7a05c03b0a31c",
    "products": [
      {
        "product": {
          "code": "COMIC_BOOKS",
          "unitValue": 15.5
        },
        "quantity": 1
      },
      {
        "product": {
          "code": "BOOKS",
          "unitValue": 9.9
        },
        "quantity": 1
      }
    ],
    "createdAt": "2024-04-12T23:47:54.008",
    "transactionId": "1712965674008_ed396176-f12a-4afc-ac54-31a4a2519f78",
    "totalAmount": 25.4,
    "totalItems": 2
  },
  "source": "ORDER_SERVICE",
  "status": "SUCCESS",
  "eventHistory": [
    {
      "source": "ORDER_SERVICE",
      "status": "SUCCESS",
      "message": "Saga Started!",
      "createdAt": "2024-04-12T23:47:54.27"
    },
    {
      "source": "PRODUCT_VALIDATION_SERVICE",
      "status": "SUCCESS",
      "message": "Products are validated successfully.",
      "createdAt": "2024-04-12T23:47:54.822"
    },
    {
      "source": "PAYMENT_SERVICE",
      "status": "SUCCESS",
      "message": "Payment realized successfully!",
      "createdAt": "2024-04-12T23:47:55.483"
    },
    {
      "source": "INVENTORY_SERVICE",
      "status": "SUCCESS",
      "message": "Inventory updated successfully!",
      "createdAt": "2024-04-12T23:47:56.037"
    },
    {
      "source": "ORDER_SERVICE",
      "status": "SUCCESS",
      "message": "Saga finished successfully!",
      "createdAt": "2024-04-12T23:47:56.157"
    }
  ],
  "createdAt": "2024-04-12T23:47:56.157"
}
```
### Acesso ao MongoDB
Para conectar-se ao MongoDB via linha de comando (cli) diretamente do ``` docker-compose ``, basta executar o comando abaixo:

``` docker exec -it order-db mongosh "mongodb://admin:123456@localhost:27017"```

Para listar os bancos de dados existentes:

```show dbs```

Para selecionar um banco de dados:

```use admin```

Para visualizar as collections do banco:

```show collections```

Para realizar queries e validar se os dados existem:

```db.order.find()```

```db.event.find()```

```db.order.find(id=ObjectId("65006786d715e21bd38d1634"))```

```db.order.find({ "products.product.code": "COMIC_BOOKS"})```

#### Referências:
Curso Udemy - Arquitetura de Microsserviços: Padrão Saga Coreografado - Aula Bônus

Autor: Victor Hugo Negrisoli

