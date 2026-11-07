# Shoploc Catalog Service

## Overview

`shoploc-catalog-service` is a backend microservice responsible for
managing the product catalog and inventory of the Shoploc platform.

It provides the APIs required to manage products, product information,
categories, and stock-related operations for merchants.

The service is part of the Shoploc microservices architecture.

## Responsibilities

The Catalog Service is responsible for:

- Managing products
- Managing product information and details
- Managing product categories
- Managing product availability
- Managing merchant inventory
- Managing stock quantities
- Providing catalog APIs to other Shoploc services
- Providing catalog data required by merchant dashboards

The Catalog Service contains catalog and inventory business logic.

Authentication and identity management are handled by
`shoploc-identity-service`.

Request routing and centralized access control are handled by
`shoploc-api-gateway`.

## Architecture

```text
                         Clients
                            │
                            ▼
                  ┌─────────────────────┐
                  │   Shoploc API       │
                  │      Gateway        │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │   Catalog Service  │
                  │                     │
                  │ Products            │
                  │ Categories          │
                  │ Inventory           │
                  │ Stock               │
                  └──────────┬──────────┘
                             │
                             ▼
                  ┌─────────────────────┐
                  │     Catalog DB      │
                  └─────────────────────┘