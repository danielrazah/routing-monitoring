# Como testar pela interface

Guia rápido para experimentar a solução pela tela: simular um cliente, vê-lo na fila/atendimento
como **agente**, trocar mensagens em tempo real e, como **admin**, acompanhar tudo. Para subir o
projeto, veja o [`COMO-SUBIR.md`](./COMO-SUBIR.md) (`docker compose up --build`).

## Antes de começar

- **Suba o stack** e abra o dashboard em <http://localhost:8090>.
- **Contas de teste** (o mesmo assunto sempre cai no mesmo time):

  | Usuário | Senha      | Perfil / time                         |
  |---------|------------|---------------------------------------|
  | `admin` | `admin123` | ADMIN — vê e opera todos os times      |
  | `ana`   | `agent123` | AGENT — Cartões                        |
  | `bruno` | `agent123` | AGENT — Cartões                        |
  | `carla` | `agent123` | AGENT — Empréstimos                    |
  | `diego` | `agent123` | AGENT — Outros                         |

- **Telas (URLs):**
  - `/` — landing (escolhe cliente ou equipe)
  - `/atendimento` — tela do **cliente** (sem login)
  - `/painel` — **dashboard** do agente/admin (exige login)

- **Dica de janelas:** o cliente **não** faz login, então use **duas janelas**: uma normal para o
  dashboard (agente/admin) e outra (pode ser a mesma janela, outra aba) em `/atendimento` para o
  cliente. Para testar **agente e admin ao mesmo tempo**, use uma janela **anônima/incógnita** para
  a segunda conta (o login fica salvo por navegador).

> Sugestão: para um fluxo previsível, use o assunto **Contratação de empréstimo** — ele vai para o
> time **Empréstimos**, que tem **um único atendente (Carla)**. Assim você sabe exatamente quem vai
> atender.

---

## 1. Simular o cliente e entrar na fila

1. Abra <http://localhost:8090/atendimento>.
2. Preencha **seu nome** (ex.: `Maria`) e escolha o assunto **Contratação de empréstimo**.
3. Clique em **Entrar na fila**.
4. Você verá um destes dois estados:
   - **"Você será atendido agora"** — havia atendente livre (foi direto para atendimento).
   - **"Você está na fila"** — o time estava lotado; assim que abrir uma vaga a tela vira
     atendimento sozinha (tempo real).

Para **simular uma fila de espera**: repita o passo acima com o mesmo assunto **4 vezes** (nomes
diferentes). Como Empréstimos tem 1 atendente × 3 vagas, do **4º cliente em diante** o estado fica
em "Você está na fila".

---

## 2. Ver o cliente como AGENTE

1. Em outra janela, abra <http://localhost:8090/painel> e entre como **`carla` / `agent123`**
   (atendente de Empréstimos).
2. No painel **Meus atendimentos**, o(s) cliente(s) em atendimento aparecem como "pastilhas" com o
   nome. Clique em um nome para abrir a conversa.
3. No card do time **Empréstimos** você vê as vagas em uso e quantos estão **na fila**. O botão
   **Atender próximo** libera uma vaga (encerra o atendimento mais antigo) e puxa o próximo da fila.

> Do lado do cliente (`/atendimento`), a tela agora mostra **quem está te atendendo** (ex.: "Carla
> está te atendendo").

---

## 3. Trocar mensagens entre cliente e agente

1. Deixe as duas janelas lado a lado: **cliente** (`/atendimento`, em atendimento) e **agente**
   (`/painel`, com a conversa daquele cliente aberta).
2. Digite uma mensagem em um lado e envie — ela aparece **no outro lado na hora** (tempo real).
   Responda pelo outro lado para ver o diálogo dos dois.
3. **Indicador de nova mensagem (piscar):** no painel do agente, selecione **outra** conversa (ou
   nenhuma). Mande uma mensagem pelo cliente: o **nome dele pisca** (com um ponto âmbar) em *Meus
   atendimentos* até você abrir a conversa.

---

## 4. Encerrar o atendimento (dos dois lados)

- **Pelo agente:** com a conversa aberta em *Meus atendimentos*, clique em **Encerrar atendimento**.
  A vaga é liberada e o próximo da fila entra automaticamente.
- **Pelo cliente:** na tela `/atendimento`, clique em **Encerrar atendimento**. O cliente sai da
  fila do agente e vê a tela de "atendimento encerrado".
- Encerrando por um lado, o outro percebe em instantes (o cliente vai para "encerrado"; o painel do
  agente remove a conversa).

---

## 5. Entrar como ADMIN e acompanhar as interações

1. Em uma janela **anônima** (para não conflitar com o login do agente), abra `/painel` e entre como
   **`admin` / `admin123`**.
2. O admin vê **todos os times** e tem, no painel:
   - **Novo atendimento** — crie contatos de qualquer assunto para popular a operação (útil para
     encher filas rapidamente sem abrir várias abas de cliente).
   - **Conversas ao vivo** — lista **todas** as conversas cliente↔agente em andamento. Clique em uma
     para acompanhar o diálogo **em tempo real** (somente leitura).
   - **Encerrar tudo** — encerra **todos** os atendimentos e **esvazia todas as filas**, zerando o
     quadro para recomeçar os testes (pede confirmação).

---

## Roteiro completo em 2 minutos

1. **Admin** (`/painel`): use **Novo atendimento** e crie 4 contatos de **Contratação de empréstimo**
   → 3 entram em atendimento, 1 fica na fila.
2. **Agente** (`carla`): em *Meus atendimentos* veja os clientes; abra uma conversa.
3. **Cliente** (`/atendimento`): entre com o mesmo assunto, veja "Carla está te atendendo" e troque
   mensagens com o agente.
4. Veja o **nome piscar** no painel do agente ao mandar mensagem com o diálogo fechado.
5. **Encerre** por um lado e veja o outro reagir; o próximo da fila entra.
6. **Admin**: acompanhe tudo em **Conversas ao vivo** e, ao final, clique em **Encerrar tudo**.

> O idioma da interface segue o do navegador (pt/en). Se algo não atualizar na hora, a tela cai em
> *polling* e reconcilia em poucos segundos.
