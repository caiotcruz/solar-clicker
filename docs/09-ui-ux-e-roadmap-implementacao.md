# Solar Clicker — Documento 9: UI/UX Spec & Roadmap de Implementação

> Status: rascunho v0.1
> Depende de: todos os documentos anteriores (01–08)

# Parte A — UI/UX Spec

## 1. Telas Principais

| Tela | Conteúdo |
|---|---|
| **Sistema Solar (tela principal)** | Sol no centro, planetas desbloqueados em órbita, HUD superior, painel lateral do planeta selecionado |
| **Painel de Planeta** | Aberto ao clicar num planeta: moeda local, upgrades locais (árvore), botão de Ascensão Local (quando disponível) |
| **Loja Geral** | Upgrades globais (doc 8, seção 5) |
| **Ascensão Geral** | Só acessível a partir do painel de Plutão quando Estabilidade Orbital atinge o requisito; mostra prévia de Pontos Estelares a ganhar antes de confirmar |
| **Loja de Ascensão Geral** | Árvore meta-progressão (doc 4, seção 6), acessível a qualquer momento (não só logo após ascender) |
| **Configurações** | Volume, estilo de notação numérica (doc 7, confirmado), tecla de atalho, etc. |

## 2. Câmera e Navegação

- **WASD:** pan lateral da câmera (`CameraController`, doc 5) para ver planetas mais distantes do Sol.
- **Zoom confirmado desde o início:** scroll do mouse (ou teclas +/-, como alternativa acessível) controla o zoom da câmera ortográfica do LibGDX (`OrthographicCamera.zoom`), com limites mínimo/máximo definidos em configuração (evita zoom infinito ou tão distante que os planetas fiquem ilegíveis). Isso substitui o que era um "fora de escopo" na v0.1 deste documento.
- Planetas ainda não desbloqueados aparecem na órbita como silhueta escurecida/sem detalhe (dá senso de "existe algo lá, ainda não é seu"), sem clique disponível.

## 3. Representação Visual do Ciclo Orbital

- **Rotação própria** do planeta: o sprite gira visualmente em sincronia com `rotationProgress` (0.0–1.0 = uma volta completa do sprite). Puramente visual, mas reforça o feedback de "está prestes a pagar" quando a rotação está quase completa.
- **Translação:** a posição do planeta na órbita se move em sincronia com `translationProgress` ao redor do Sol.
- **Evento de pagamento:** ao completar uma rotação/translação (`RotationCompletedEvent`/`TranslationCompletedEvent`, doc 5), a UI dispara um efeito visual curto (destaque no planeta + número flutuante da moeda ganha) — a UI é 100% reativa a esses eventos, nunca "adivinha" quando um pagamento ocorreu.
- **Barra de progresso** (doc 8) sutil sob/sobre o planeta, mostrando visualmente o quão perto está do próximo pagamento — importante especialmente para planetas de ciclo longo (Netuno, Plutão), onde o jogador precisa de algum feedback entre um pagamento e outro.

## 4. HUD

- **Topo:** Solar Coins (formatado via `BigNumberFormatter`, doc 7) sempre visível.
- **Lateral (quando um planeta está selecionado):** moeda local daquele planeta, produção por clique, tempo restante estimado até próxima rotação/translação.
- **Notificação de progresso offline:** ao abrir o jogo, um resumo modal simples ("Enquanto você esteve fora: +X") antes de qualquer outra interação — não bloqueante além de um clique de dispensar.

## 5. Uso dos Sprites Pixel Art

- Cada planeta usa um **texture atlas** (LibGDX `TextureAtlas`) com, no mínimo: sprite estático (idle), possivelmente frames de animação simples se você já tiver (ex: leve "respiração"/brilho do Sol). Se não houver frames extras, o sprite estático rotacionando via transform já entrega a sensação de rotação sem precisar de spritesheet de animação.
- Silhueta de planeta bloqueado: pode ser o mesmo sprite com um shader simples de escurecimento/dessaturação (LibGDX suporta isso nativamente), evitando precisar de uma sprite extra "bloqueada" pra cada planeta.

## 6. Considerações para Steam

- Janela redimensionável com escala consistente de UI (comum em jogos Steam — jogadores usam resoluções muito variadas).
- Sem necessidade de suporte a controle (idle/clicker é mouse+teclado por natureza), mas isso pode ser revisitado se você mirar Steam Deck (que roda bem jogos mouse/teclado emulados, então não é bloqueante).
- Popups de conquista e indicador de Cloud Save são tratados automaticamente pelo overlay da Steam via `steamworks4j` (doc 6) — não exigem UI própria nossa.

---

# Parte B — Roadmap de Implementação

## Fase 0 — Setup

- Estrutura de projeto LibGDX conforme pacotes do doc 5.
- `BigNumber` (doc 7) implementado e coberto por testes unitários **antes de qualquer outra coisa** — é a fundação de todo o resto.
- Scaffolding de i18n (`I18NBundle`, arquivos `en.properties`/`pt_BR.properties` vazios/mínimos) montado desde já — mais barato garantir que nenhum texto seja hardcoded desde o primeiro dia do que migrar depois.
- `CameraController` já nasce com pan (WASD) **e** zoom (scroll/+-), mesmo que o MVP ainda tenha poucos planetas pra navegar.

## Fase 1 — MVP (conforme escopo definido no doc 1, seção 7)

- Sol clicável + Solar Coins.
- Mercúrio, Vênus, Terra desbloqueáveis (3 focos diferentes: generalista/clique/idle).
- Loja Geral básica.
- Save/Load simples (schema v1 do doc 6, sem migração ainda — não há versão anterior).
- **Sem** sistema de ciclo orbital completo ainda nesta fase — produção pode ser simplificada (ex: só clique + um "idle por segundo" temporário) só para validar o core loop rapidamente.

## Fase 2 — Sistema de Ciclos e Economia Completa

- `OrbitalCycle` completo (doc 8, seção 2), substituindo a produção "idle por segundo" temporária da Fase 1 pelo modelo real de pagamento por ciclo (doc 2, seção 7).
- `EventBus` e eventos de ciclo completo.
- `OfflineProgressCalculator` com cap + penalidade (doc 8, seção 4).
- `ModifierRegistry` (interdependência entre planetas, doc 2 seção 5) — ainda com poucos planetas pra validar o conceito antes de escalar pros 9.

## Fase 3 — Planetas Restantes

- Marte, Júpiter, Saturno, Urano, Netuno, Plutão, cada um com seu `PlanetFocus` (doc 8, seção 3) e upgrades locais (doc 3).
- Validação de que a interdependência (Netuno buffando outros, Marte reduzindo custo de upgrade de planeta à escolha) funciona como esperado.

## Fase 4 — Ascensão Local

- `LocalAscension` para todos os planetas (doc 4, seção 2), incluindo o caso especial de Plutão (Estabilidade Orbital, doc 3 seção 10).

## Fase 5 — Ascensão Geral

- `GeneralAscension`, cálculo de Pontos Estelares, Loja de Ascensão Geral completa (doc 4, seção 6) — incluindo os nós "Sono Estelar" e "Vigília Eficiente".
- Este é o ponto em que o **loop completo do jogo** (run → ascender → run mais forte) existe pela primeira vez de ponta a ponta.

## Fase 6 — Polish de UI/UX

- Integração completa dos sprites pixel art (Parte A deste documento).
- Feedback visual de pagamento de ciclo, barras de progresso, HUD final.
- Tela de Configurações (estilo de notação numérica, troca manual de idioma, limites/sensibilidade de zoom).
- Tradução completa (EN/PT-BR) de todo texto acumulado até aqui — a infraestrutura já existe desde a Fase 0, esta fase é sobre preencher o conteúdo.

## Fase 7 — Integração Steam

- `steamworks4j`: achievements (usando o bloco `statistics` do save, doc 6), Steam Cloud (validando a escrita atômica do doc 6 em condições reais), empacotamento via `jpackage` para instalador nativo.

## Fase 8 — Balanceamento e Playtest

- Execução do checklist do doc 10, ajuste dos valores numéricos que ficaram marcados como "pendente de calibragem" em todos os documentos anteriores (`custoBase`, `crescimentoLocal`, `C` da fórmula de Pontos Estelares, tetos da Loja de Ascensão Geral, cap/penalidade offline, etc.).

## Fase 9 — Preparação de Lançamento

- Assets de loja Steam (capa, screenshots, trailer) — fora do escopo técnico deste conjunto de documentos, mas listado para não ser esquecido no planejamento geral.

---

## 7. Internacionalização (i18n)

**Decisão confirmada:** lançamento com Português e Inglês desde o início, **idioma padrão (primeira abertura) em Inglês** — o jogador pode trocar depois em Configurações.

- Todo texto de UI (labels, nomes de upgrade, descrições, mensagens de sistema) é referenciado por **chave**, nunca hardcoded — ex: `shop.upgrade.click_power.name` — resolvida em tempo de execução a partir de um arquivo de idioma (`i18n/en.properties`, `i18n/pt_BR.properties`, formato padrão de `ResourceBundle` do Java, que o LibGDX suporta nativamente via `I18NBundle`).
- **Nenhum texto de gameplay mora na camada `domain`** (doc 5) — o `domain` trabalha só com **ids** (ex: `"venus"`, `"click_power"`); a tradução do id pra texto exibível é responsabilidade exclusiva da camada `render`/`ui`. Isso significa que adicionar um terceiro idioma no futuro é só adicionar um novo arquivo de propriedades, sem tocar em lógica de jogo.
- Detecção de idioma do sistema operacional pode ser usada como sugestão inicial (ex: se o SO estiver em português, sugerir PT-BR na primeira abertura), mas o **padrão contratual** caso não haja preferência é Inglês, conforme decidido.

## Decisões Confirmadas (v0.2)

1. Idiomas: Português e Inglês desde o lançamento, padrão Inglês na primeira abertura — ver seção 7 acima.
2. Zoom de câmera: incluído desde o início — ver seção 2 acima.

---
**Próximo (e último) documento do roadmap original:** `10-checklist-balanceamento-testes.md` — checklist de playtest e balanceamento, fechando a documentação inicial do projeto.
