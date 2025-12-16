# 🚀 Loomi Desafio - Order Processing System


Sistema de processamento de pedidos **geolocalizado** com alta performance, idempotência.

## 🎯 **Visão Geral**

- **Order Processing System**: Recebe pedidos via REST → valida → publica Kafka → processa assíncrono
- **Product Catalog**: Catálogo com estoque real-time (PostgreSQL)
- **Kafka (Redpanda)**: Eventos principais `order-events-created` → `order-events-processed`
- **Integração E2E**: 100% testada com Testcontainers


## 📋 **Requisitos**

| Ferramenta | Versão Mínima | Link Download |
| :-- | :-- | :-- |
| **Java** | 17+ (OpenJDK) | [Adoptium](https://adoptium.net/) |
| **Maven** | 3.9+ | [Maven](https://maven.apache.org/install.html) |
| **Docker** | 24+ + Docker Compose | [Docker Desktop](https://docker.com) |
| **Make** | Qualquer | [Linux/Mac nativo](https://www.gnu.org/software/make/) / [Windows WSL](https://learn.microsoft.com/en-us/windows/wsl/) |

**Windows**: Use **WSL2 Ubuntu** ou **Git Bash** com Make instalado.

## 🚀 **Execução Rápida (1 comando)**

```bash
git clone https://github.com/gustalencarvalho/loomi-desafio.git
cd loomi-desafio
make up
```

**✅ 3 minutos → Sistema 100% funcional!**

## ⚙️ **Comandos Makefile**

```bash
# 🚀 Inicia TUDO (build + testes + Docker)
make up

# 🧪 Testes unitários + integração
make test

# 🛑 Para containers
make down

# 📋 Ajuda completa
make help
```


## 🐳 **Containers Criados**

| Container | Porta | Função | Healthcheck |
| :-- | :-- | :-- | :-- |
| `product-db` | 5432 | Catálogo produtos | PostgreSQL ready |
| `order-db` | 5433 | Pedidos | PostgreSQL ready |
| **`redis`** | **6379** | **Idempotência** | **redis-cli ping** |
| `kafka` | 9092 | Eventos | rpk cluster health |
| `product-service` | **8081** | **Catálogo REST** | **Actuator health** |
| **`order-service`** | **8080** | **API Principal** | **Actuator health** |
| `redpanda-console` | **8085** | Kafka GUI | - |

## 🧪 **Testes (Cobertura)**

```bash
# Todos testes (unit + integração)
make test

# Testes específicos
mvn test -Dtest=OrderCreateIntegrationTest
mvn test -Dtest=OrderFailureIntegrationTest

# Cobertura relatório
# Abra: target/site/jacoco/index.html
```

**✅ 50+ testes**: Happy path, edge cases, falhas, volumes, corporate.

## 🛠️ **Funcionalidades Implementadas**


### ✅ **Resiliência**

```
🛡️ Cobertura testes
📊 Actuator metrics + health
```

## 🔍 **Monitoramento**

```
🌐 APIs: http://localhost:8080
📊 Product: http://localhost:8081
🕹️ Kafka GUI: http://localhost:8085
🐳 Docker: docker compose logs -f
🏥 Health: http://localhost:8080/actuator/health
```

```

## 🎉 **Pronto para Produção!**

```
✅ 100% Dockerizado
✅ Testado E2E (Testcontainers)
✅ Healthchecks
```

**`make up` → Production-ready em 3min!** 🚀

***

**👨‍💻 Autor**: Gustavo Alencar
**📧 Contato**: gustalencarvalho@gmail.com
**🔗 GitHub**: [gustalencarvalho/loomi-desafio](https://github.com/gustalencarvalho/loomi-desafio)
```