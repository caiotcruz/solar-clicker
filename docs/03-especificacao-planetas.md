# Solar Clicker — Documento 3: Especificação dos Planetas

> Status: rascunho v0.1
> Depende de: `01-GDD-visao-geral.md`, `02-economia-balanceamento.md`
> Nota: custos de desbloqueio abaixo são **multiplicadores relativos** (não valores finais de jogo) — a calibragem exata é feita em playtest (ver doc 10, Checklist de Balanceamento).

## 0. Visão Geral da Ordem

| # | Corpo | Foco | Custo relativo de desbloqueio* |
|---|---|---|---|
| 0 | Sol | Fonte inicial (sempre ativo) | — (já desbloqueado no início) |
| 1 | Mercúrio | Generalista fraco | 1x |
| 2 | Vênus | Clique | 8x |
| 3 | Terra | Idle | 50x |
| 4 | Marte | Upgrades | 300x |
| 5 | Júpiter | Escala | 2.000x |
| 6 | Saturno | Automação | 15.000x |
| 7 | Urano | Eficiência | 120.000x |
| 8 | Netuno | Buff a outros planetas | 1.000.000x |
| 9 | Plutão | Preparação para Ascensão Geral | 10.000.000x |

*Relativo ao custo de desbloqueio de Mercúrio = 1x. Moeda usada para desbloqueio: Solar Coins em todos os casos (planetas são um "gate" da moeda geral, não das moedas locais).

---

## 1. Sol

- **Papel:** fonte inicial, sempre clicável, nunca é "desbloqueado" — já está ativo no início do jogo.
- **Produção:** só existe produção por clique (sem idle próprio, sem rotação/translação — narrativamente o Sol não orbita nada). Gera Solar Coins diretamente.
- **Upgrades:** ficam na Loja Geral (ex: poder de clique base, crítico de clique), não numa árvore própria do Sol.
- **Sem ascensão local própria** — o Sol participa apenas da Ascensão Geral.

## 2. Mercúrio — Generalista Fraco

- **Descrição:** primeiro planeta, faz um pouco de tudo (clique, idle, upgrade) mas com eficiência baixa em cada frente. É o planeta "tutorial".
- **Mecânica de foco:** não tem bônus especial em nenhuma categoria — serve para ensinar todos os sistemas (clique local, rotação, translação, upgrade local) de forma simples.
- **Upgrades locais (exemplos):**
  - Poder de clique de Mercúrio (+X flat)
  - Rotação mais rápida de Mercúrio (-X% tempo de rotação)
  - Colheita de rotação maior (+X% moeda por rotação)
- **Rotação/Translação:** rotação rápida (~9,8 min) — o jogador vê pagamentos frequentes, bom para ensinar o sistema de ciclo.
- **Ascensão Local:** reseta upgrades e moeda de Mercúrio; concede um multiplicador permanente pequeno e genérico (+% em toda produção de Mercúrio pós-reset). Serve mais como tutorial de ascensão do que como sistema profundo.

## 3. Vênus — Clique

- **Descrição:** planeta voltado a recompensar cliques ativos — jogo mais "arcade" quando o jogador está com atenção na tela.
- **Mecânica de foco:** clique em Vênus tem poder-base bem mais alto que idle; upgrades priorizam multiplicadores de clique e chance/efeito de crítico.
- **Upgrades locais (exemplos):**
  - Multiplicador de poder de clique
  - Chance de clique crítico (paga um "mini-bônus" instantâneo, similar a completar uma mini-rotação)
  - Redução de tempo de rotação (mais ciclos = mais oportunidades de clique bonus)
- **Rotação/Translação:** como o foco é clique, a colheita de rotação de Vênus pode receber bônus adicional se o jogador clicou ativamente durante aquele ciclo (incentivo a jogar ativamente em Vênus).
- **Ascensão Local:** reseta upgrades de clique; concede multiplicador permanente ao poder de clique (inclusive clique no Sol, para dar sensação de progresso cross-planeta).

## 4. Terra — Idle

- **Descrição:** o planeta "de casa" — foco total em produção passiva, o jogador não precisa interagir para progredir.
- **Mecânica de foco:** colheita de rotação e translação com valores-base mais altos que os outros planetas de nível equivalente; upgrades priorizam produção passiva e velocidade de ciclo.
- **Upgrades locais (exemplos):**
  - Multiplicador de colheita de rotação
  - Multiplicador de colheita de translação
  - Redução de tempo de translação (mais "anos" completados por hora real)
- **Rotação/Translação:** é a referência do jogo (10s / 60min) — nenhum ajuste especial de fórmula, só bônus de valor.
- **Ascensão Local:** reseta upgrades da Terra; concede multiplicador permanente à produção idle **global** (não só da Terra) — reforça a identidade "idle" do planeta.

## 5. Marte — Upgrades

- **Descrição:** planeta focado em tornar upgrades (de qualquer planeta) mais baratos e mais fortes por nível.
- **Mecânica de foco:** upgrades de Marte não aumentam produção diretamente — reduzem custo de upgrade (%) ou aumentam o efeito por nível de upgrades de outros planetas.
- **Upgrades locais (exemplos):**
  - Redução de custo de upgrades locais de Marte
  - Redução de custo de upgrades de **outro planeta à escolha** (jogador define o alvo)
  - Aumento do efeito por nível de todos os upgrades ativos
- **Rotação/Translação:** colheita padrão, sem regra especial.
- **Ascensão Local:** reseta upgrades de Marte; concede redução permanente e global no custo de upgrades (de todos os planetas).

## 6. Júpiter — Escala

- **Descrição:** planeta de "saltos grandes" — em vez de muitos upgrades pequenos incrementais, poucos upgrades com efeito multiplicativo alto.
- **Mecânica de foco:** upgrades caros, mas cada nível multiplica (não soma) a produção — poucas escolhas, mas decisões de alto impacto.
- **Upgrades locais (exemplos):**
  - "Camadas" de escala (cada camada multiplica toda produção de Júpiter por um fator alto)
  - Desbloqueio de tier de escala seguinte (gate, não incremental)
- **Rotação/Translação:** colheitas com valores-base já grandes (reflete o foco em escala).
- **Ascensão Local:** reseta as camadas de escala; concede um multiplicador permanente que afeta o **custo-benefício de escalar** em runs futuras (upgrades de escala ficam proporcionalmente mais baratos).

## 7. Saturno — Automação

- **Descrição:** planeta que reduz a necessidade de gerenciamento manual — compra automática, coleta automática.
- **Mecânica de foco:** upgrades desbloqueiam automação (auto-compra de upgrades baratos em outros planetas, auto-coleta de colheitas de rotação/translação quando o jogador está offline).
- **Upgrades locais (exemplos):**
  - Auto-compra de upgrades até um teto de custo definido pelo jogador
  - Auto-coleta de colheitas de rotação/translação (reduz "perda" quando offline)
  - Velocidade da automação (menos delay entre ações automáticas)
- **Rotação/Translação:** colheita padrão.
- **Ascensão Local:** reseta upgrades de automação; concede automação permanente básica mesmo em runs futuras antes de comprar de novo (ex: auto-coleta sempre ativa desde o início pós-ascensão).

## 8. Urano — Eficiência

- **Descrição:** planeta que não aumenta produção diretamente, mas reduz desperdício em todo o sistema.
- **Mecânica de foco:** upgrades reduzem custo de desbloqueio de planetas, aumentam taxa de exportação de moeda local → Solar Coins, e reduzem "perda" na conversão entre sistemas.
- **Upgrades locais (exemplos):**
  - Redução do custo de desbloqueio do próximo planeta
  - Aumento da taxa de exportação de moeda local para Solar Coins
  - Redução do tempo necessário de translação para gerar a colheita grande
- **Rotação/Translação:** colheita padrão.
- **Ascensão Local:** reseta upgrades de eficiência; concede redução permanente no custo de desbloqueio de planetas em runs futuras.

## 9. Netuno — Buff a Outros Planetas

- **Descrição:** planeta de suporte puro — fraco sozinho, mas essencial para uma build otimizada.
- **Mecânica de foco:** upgrades de Netuno não afetam Netuno; usam o **Registro Global de Modificadores** (doc 2, seção 5) para aplicar bônus percentuais de produção a todos os outros planetas.
- **Upgrades locais (exemplos):**
  - Bônus percentual de produção para todos os planetas (rotação e translação)
  - Bônus específico para o planeta de foco "idle" (Terra) ou "clique" (Vênus), à escolha do jogador
  - Redução do tempo de ciclo (rotação/translação) de todos os planetas em uma pequena %
- **Rotação/Translação:** colheita própria baixa (é o planeta mais "fraco" em produção direta, de propósito).
- **Ascensão Local:** reseta os buffs; concede um multiplicador permanente na força dos buffs concedidos a outros planetas (Netuno pós-ascensão "ajuda mais" que antes).

## 10. Plutão — Preparação para Ascensão Geral

- **Descrição:** planeta final do ciclo, não existe para gerar economia relevante — existe para **destravar a Ascensão Geral**.
- **Mecânica de foco:** upgrades locais de Plutão geram um recurso especial, **Estabilidade Orbital**, em vez de contribuir para Solar Coins. Estabilidade Orbital tem uma única função: preencher a condição para acionar a Ascensão Geral.
- **Upgrades locais (exemplos):**
  - Geração de Estabilidade Orbital (por rotação e por translação)
  - Redução do total de Estabilidade Orbital necessário para acionar a Ascensão Geral
- **Rotação/Translação:** translação de Plutão é a mais longa do jogo (~10 dias corridos) — reflete que este é o "planeta de fim de ciclo", o jogador não precisa completar uma translação inteira todo run; a rotação (mais curta) já entrega progresso relevante de Estabilidade Orbital.
- **Ascensão Local (Plutão) — caso especial:** ao contrário dos outros planetas, a "ascensão local" de Plutão **é o próprio ato de acionar a Ascensão Geral**. Não existe uma ascensão de Plutão separada da Ascensão Geral — completar a árvore de Plutão = liberar o botão de Ascensão Geral.

---

## 11. Nota sobre a Ascensão Geral

A Ascensão Geral (detalhada no próximo documento) é acionada a partir de Plutão e reseta:
- Todos os planetas desbloqueados (voltam a precisar ser desbloqueados)
- Todas as moedas locais e Solar Coins
- Todas as ascensões locais de cada planeta

E concede, permanentemente:
- Um multiplicador global de produção (afeta cliques, rotação e translação de todos os planetas)
- Redução permanente no custo de desbloqueio de planetas (empilha com o efeito de Urano, mas em separado — este é o efeito "de prestígio", aquele é "de gameplay dentro da run")

---
**Próximo documento:** `04-sistema-ascensao.md` — regras detalhadas de Ascensão Local (fórmulas de reset/bônus) e Ascensão Geral (fórmula de multiplicador por número de ascensões, curva de retorno).
