# 🔗 Guia de Conexão Frontend (Vercel) ↔ Backend (Render)

## ✅ Status do Backend

### Configuração CORS ✅
O backend está **configurado e pronto** para aceitar requisições do Vercel:

- ✅ Aceita `https://northern-lights-frontend-2i36.vercel.app`
- ✅ Aceita todos os domínios `*.vercel.app` (preview e production)
- ✅ Aceita localhost para desenvolvimento
- ✅ Configurado com `allowCredentials(true)` para JWT
- ✅ Commit realizado e push feito para o repositório

### URL do Backend no Render
**URL Base:** `https://northern-lights-api.onrender.com` (ou a URL específica do seu serviço no Render)

---

## 📋 O que fazer no Frontend (Vercel)

### 1. Atualizar `src/services/api.js`

**Localização:** `NORTHERN LIGHTS-Front/aurora-learn-suite/src/services/api.js`

**Mudança na linha 2:**

```javascript
// ANTES:
const API_BASE_URL = 'http://localhost:8080';

// DEPOIS:
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
```

### 2. Atualizar `src/pages/Register.tsx`

**Localização:** `NORTHERN LIGHTS-Front/aurora-learn-suite/src/pages/Register.tsx`

**Adicionar no início da função (após linha 70):**

```typescript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';
```

**Substituir as linhas 79 e 85:**

```typescript
// ANTES (linha 79):
response = await fetch('http://localhost:8080/api/auth/register/student', {

// DEPOIS:
response = await fetch(`${API_BASE_URL}/api/auth/register/student`, {
```

```typescript
// ANTES (linha 85):
response = await fetch('http://localhost:8080/api/auth/register/teacher', {

// DEPOIS:
response = await fetch(`${API_BASE_URL}/api/auth/register/teacher`, {
```

### 3. Criar arquivo `.env` (desenvolvimento local)

**Localização:** `NORTHERN LIGHTS-Front/aurora-learn-suite/.env`

```bash
VITE_API_BASE_URL=http://localhost:8080
```

### 4. Criar arquivo `.env.example` (template)

**Localização:** `NORTHERN LIGHTS-Front/aurora-learn-suite/.env.example`

```bash
VITE_API_BASE_URL=http://localhost:8080
```

### 5. Verificar `.gitignore`

Certifique-se de que `.env` está no `.gitignore`:

```gitignore
# .env files
.env
.env.local
.env.production
```

---

## ⚙️ Configuração no Vercel

### Passo a Passo:

1. **Acesse o Dashboard do Vercel:**
   - https://vercel.com
   - Faça login e selecione seu projeto

2. **Vá em Settings:**
   - Clique no projeto → **Settings** (no menu lateral)

3. **Environment Variables:**
   - Clique em **Environment Variables** (no menu lateral esquerdo)

4. **Adicionar Variável:**
   - **Key:** `VITE_API_BASE_URL`
   - **Value:** `https://northern-lights-api.onrender.com` (ou a URL do seu backend no Render)
   - **Environment:** Selecione:
     - ✅ Production
     - ✅ Preview
     - ✅ Development (opcional)
   - Clique em **Save**

5. **Fazer Deploy:**
   - Após adicionar a variável, faça um novo deploy:
     - Vá em **Deployments**
     - Clique nos três pontos (...) do último deployment
     - Selecione **Redeploy**
   - Ou faça um novo commit/push que acionará o deploy automático

---

## 🧪 Testes

### Teste Local:
1. Certifique-se de que o backend está rodando em `http://localhost:8080`
2. Execute o frontend: `npm run dev`
3. Verifique no DevTools (F12 → Network) que as requisições vão para `http://localhost:8080`

### Teste em Produção (Vercel):
1. Acesse: `https://northern-lights-frontend-2i36.vercel.app`
2. Abra o DevTools (F12 → Network)
3. Faça uma requisição (ex: login/registro)
4. Verifique que a requisição vai para `https://northern-lights-api.onrender.com`

---

## 📝 Checklist Final

### Backend ✅
- [x] CORS configurado para aceitar Vercel
- [x] Commit realizado
- [x] Push para repositório feito
- [x] Deploy no Render (automático ou manual)

### Frontend
- [ ] Atualizar `api.js` com variável de ambiente
- [ ] Atualizar `Register.tsx` com variável de ambiente
- [ ] Criar `.env` local
- [ ] Criar `.env.example`
- [ ] Verificar `.gitignore`
- [ ] Fazer commit das mudanças
- [ ] Fazer push para repositório
- [ ] Configurar variável no Vercel
- [ ] Fazer deploy no Vercel
- [ ] Testar em produção

---

## 🔍 Troubleshooting

### Erro: "CORS policy: No 'Access-Control-Allow-Origin' header"
- ✅ **Solução:** O backend já está configurado. Verifique se o deploy no Render foi concluído.

### Erro: "Network Error" ou "Failed to fetch"
- Verifique se a URL do backend no Render está correta
- Verifique se o backend está online (acesse a URL no navegador)
- Verifique se a variável `VITE_API_BASE_URL` está configurada no Vercel

### Requisições ainda vão para localhost em produção
- Verifique se a variável `VITE_API_BASE_URL` está configurada no Vercel
- Faça um novo deploy após adicionar a variável
- Limpe o cache do navegador (Ctrl+Shift+R)

---

## 📞 Informações Importantes

- **Frontend URL:** `https://northern-lights-frontend-2i36.vercel.app`
- **Backend URL:** `https://northern-lights-api.onrender.com` (verificar no Render)
- **Variável de Ambiente:** `VITE_API_BASE_URL`

---

**Última atualização:** 2025-01-25  
**Status Backend:** ✅ Configurado e pronto  
**Status Frontend:** ⏳ Aguardando configuração

