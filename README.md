# HH-CRM-V001

# Ledgr

**A white-label, multi-tenant CRM and operations platform for trade-driven businesses.**

> Working name: Ledgr, placeholder until a final brand is confirmed. Swap freely.

## What This Is

Ledgr is a configurable CRM and back-office platform built to manage the full lifecycle of a trading, logistics, or resource-based business: deals, clients, inventory, fleet/logistics, invoicing, documents, and a complete audit trail of every action taken in the system.

It was originally built to run Highlands Holdings' chrome concentrate trading operation, but the architecture is industry-agnostic and multi-tenant from the ground up. Any company in a similar operational category can onboard as its own isolated tenant, apply its own branding (name, logo, color scheme), and configure the platform's fields, categories, and workflows to match its business, without touching the underlying codebase.

## Why We're Building This

Operators across the spectrum, from micro and small traders to mid-market players to major corporations, in trading, logistics, and resource sectors are stuck choosing between:
- Generic CRMs (Salesforce, HubSpot, Zoho) that don't understand deal structures, commodity grading, or logistics-heavy workflows, and require expensive customization
- Spreadsheets and manual paper trails, with no audit history, no KYC tracking, and no real-time visibility across deals and inventory

Ledgr closes that gap: a purpose-built, deal-centric operations tool that's configurable and ready to deploy as a branded product, scaling from micro-operators to enterprise-level corporations.

## Core Capabilities

- **Deals**: full transaction lifecycle management, from negotiation to close
- **Clients**: contact and relationship management with built-in KYC tracking
- **Inventory**: commodity/product tracking with cascading category-to-grade (or category-to-spec) logic
- **Logistics & Fleet**: trucks, drivers, trips, and delivery tracking
- **Invoicing**: generate and track invoices tied directly to deals
- **Document Management**: upload and attach supporting documents (contracts, KYC docs, certificates, etc.) to any record
- **Audit Trail**: a complete, tamper-evident log of who created, edited, or deleted every record
- **Multi-Tenant Architecture**: each business operates in its own isolated instance, with its own branding, users, and data
- **Configurable Fields**: categories, statuses, and dropdown values are tenant-configurable rather than hardcoded, so each vertical can adapt the platform to its own terminology

## Who This Is For (Target Verticals)

Ledgr was initially built for a commodities trading operation. Its data model (deals, clients, inventory, logistics, invoicing, audit) generalizes to any entity, from micro-enterprises to major corporations, operating in a deal-, inventory-, or logistics-driven sector, including:

- **Mining & Metals Trading**: chrome, gold, coal, and other bulk commodity trading operations
- **Commodities Trading**: brokers and traders across metals, energy, and soft commodities
- **Agricultural Commodity Trading**: grain, produce, and soft commodity trading houses
- **Import/Export & Freight Forwarding**: cross-border deals with heavy logistics and documentation needs
- **Wholesale & B2B Distribution**: bulk inventory, client accounts, and fleet-based delivery
- **Fuel & Energy Trading**: physical product movement, grading/spec tracking, and compliance documentation
- **Construction & Building Materials Supply**: bulk material sales, client accounts, and delivery logistics
- **Manufacturing & Wholesale Suppliers**: client deals, raw material inventory, and dispatch
- **Real Estate & Property Trading**: deal pipelines, client/KYC management, and document-heavy transactions
- **Professional Services & Consultancies**: structured client and deal pipeline tracking with audit compliance
- **Any relationship- and deal-driven business, micro to macro**, needing deal tracking, inventory, logistics, and audit compliance in one place, without building custom software from scratch

## Business Model

Ledgr is delivered as a SaaS product. Businesses onboard as tenants, apply their own branding, and pay a subscription (or setup plus subscription) fee for access to a configured instance. This turns a single custom build for Highlands Holdings into a repeatable, licensable product for operators across the verticals above.

## Status

Currently in active backend development, transitioning from a UAT-only frontend (browser local storage) to a real multi-tenant backend with proper authentication, data isolation per tenant, and white-label configuration support.
