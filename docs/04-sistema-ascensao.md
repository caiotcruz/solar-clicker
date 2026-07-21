# Solar Clicker — Documento 4: Sistema de Ascensão

> Status: rascunho v0.1
> Depende de: `02-economia-balanceamento.md`, `03-especificacao-planetas.md`

## 1. Visão Geral — Duas Camadas de Ascensão

1. **Ascensão Local** (por planeta): reset pequeno, frequente, dá um multiplicador permanente *daquele planeta* (ou, em alguns casos especiais como a Terra e Marte, um multiplicador que vaza para o sistema todo — ver doc 3).
2. **Ascensão Geral** (só em Plutão): reset grande, raro, zera literalmente tudo — incluindo as ascensões locais — mas concede **Pontos Estelares**, a moeda de prestígio permanente que nunca é resetada, gastável numa loja de upgrades meta.

```
Run normal → Ascensões Locais (várias, pequenas) → Plutão pronto → Ascensão Geral → NOVA run (mais forte)
```

## 2. Ascensão Local — Regras Gerais

**Condição de gatilho (nível n → n+1):**
```
requisitoAscensaoLocal(planeta, n) = requisitoBase(planeta) * crescimentoLocal(planeta)^n
```
- `requisitoBase` e `crescimentoLocal` são valores por planeta, calibrados em playtest (ver doc 10).
- O requisito é medido em **moeda local acumulada na run atual** (lifetime daquela run, não saldo atual — assim gastar em upgrades antes de ascender não atrasa o progresso rumo à ascensão).

**O que reseta:**
- Saldo atual da moeda local do planeta
- Níveis de todos os upgrades locais do planeta

**O que NÃO reseta:**
- O nível de Ascensão Local em si (ele só sobe)
- Estado de desbloqueio do planeta (o planeta continua desbloqueado)

**Bônus concedido (genérico, especializado por planeta conforme doc 3):**
```
bonusLocal(planeta, nivelAscensao) = 1 + fatorBonus(planeta) * nivelAscensao^0.8
```
- Expoente `0.8` (sublinear) para ascensões locais sempre valerem a pena, mas sem crescimento explosivo — o crescimento "explosivo" fica reservado pra Ascensão Geral.
- `fatorBonus` também é por planeta (Netuno, por ex., deve ter `fatorBonus` que afeta a força dos *buffs a outros*, não a própria produção — ver doc 3, seção 9).

## 3. Ascensão Geral — Condição de Gatilho

Só pode ser acionada a partir de Plutão, ao acumular **Estabilidade Orbital** suficiente:
```
requisitoEstabilidade(k) = requisitoBaseEstabilidade * crescimentoGeral^k
```
- `k` = número de Ascensões Gerais já realizadas (a run 1 tem o requisito mais baixo; cada ascensão subsequente pede mais, mas o jogador também está proporcionalmente mais forte — ver seção 5, loja meta).

## 4. O Que a Ascensão Geral Reseta / Preserva

**Reseta:**
- Desbloqueio de todos os planetas (voltam a ficar bloqueados, exceto o Sol)
- Todas as moedas (Solar Coins + todas as moedas locais)
- Todos os níveis de upgrade local
- Todos os níveis de Ascensão Local de todos os planetas (voltam a 0)
- Estabilidade Orbital acumulada

**Preserva (permanente):**
- **Pontos Estelares** totais (moeda de prestígio, nunca gasta "sozinha" — ver seção 5)
- Upgrades já comprados na **Loja de Ascensão Geral** (meta-progressão)
- Estatísticas históricas (para conquistas / telemetria — sem efeito em gameplay)

## 5. Pontos Estelares — Fórmula de Ganho

Calculados **no momento em que a Ascensão Geral é acionada**, com base no total de Solar Coins acumulados na run que está terminando (lifetime da run, incluindo os já gastos):

```
pontosEstelares(run) = piso( C * (totalSolarCoinsAcumuladosNaRun) ^ 0.5 )
```
- Expoente `0.5` (raiz quadrada): padrão de mercado em idle games para prestígio — dá retorno decrescente por run, incentivando o jogador a "terminar de subir" antes de resetar, mas sem tornar cada run seguinte trivial.
- `C` é uma constante de calibragem (doc 10).
- Pontos Estelares de cada Ascensão Geral **somam** ao total acumulado do jogador — nunca são subtraídos, exceto pelo que for de fato "gasto" na Loja de Ascensão Geral (que, uma vez comprado, o upgrade fica permanente — então o "gasto" é conceitual, não um saque real de saldo reutilizável).

## 6. Loja de Ascensão Geral (Meta-Progressão)

Comprada com Pontos Estelares, é a única progressão que sobrevive a **todas** as Ascensões Gerais seguintes. Proposta inicial de árvore (nós largos, sem sub-ramificações profundas nesta fase — profundidade pode crescer em versões futuras):

| Upgrade | Efeito | Escala |
|---|---|---|
| **Brilho Estelar** | +X% produção global (cliques + rotação + translação, todos os planetas) | Linear por ponto investido |
| **Órbitas Eficientes** | -X% custo de desbloqueio de planetas | Linear, com teto (não pode zerar o custo) |
| **Engenharia Ancestral** | -X% custo de upgrades locais em todos os planetas | Linear, com teto |
| **Estabilidade Herdada** | Começa cada run com uma quantidade inicial de Estabilidade Orbital (menos grind até a próxima Ascensão Geral) | Escala com nível investido |
| **Automação Primordial** | Desbloqueia automação básica (estilo Saturno) desde o início da run, mesmo antes de desbloquear Saturno | Nó único (liga/desliga, sem níveis) |
| **Ciclos Acelerados** | +X% velocidade de rotação/translação de todos os planetas | Linear, com teto (evita quebrar o pacing do jogo) |
| **Sono Estelar** | Aumenta o limite de tempo (cap) considerado no cálculo de progresso offline (doc 8, seção 4) | Linear por ponto investido, com teto alto (não elimina o cap totalmente — sempre há algum limite, ver nota abaixo) |
| **Vigília Eficiente** | Reduz a penalidade percentual aplicada à produção offline, aproximando-a de 100% | Linear, com teto em (mas nunca ultrapassando) 100% da produção online |

**Nota de design — "build offline":** investir pesado em **Sono Estelar** + **Vigília Eficiente** ao longo de várias Ascensões Gerais cria uma build alternativa viável de jogar majoritariamente offline/AFK — mais lenta de montar que as builds convencionais (exige várias runs de investimento acumulado em Pontos Estelares só nesses dois nós), mas é uma escolha legítima de longo prazo, não um exploit. Nenhum planeta precisa ter seu foco redesenhado para isso — a build "offline" vive inteiramente na camada de meta-progressão.

Todos os upgrades desta loja são **permanentes e cumulativos entre Ascensões Gerais** — é a única fonte real de "ficar mais forte para sempre" no jogo.

## 7. Considerações de Curva / Diminishing Returns

- Ascensão Local: crescimento sublinear (expoente 0.8) — sempre vale a pena, nunca broken.
- Ascensão Geral: crescimento via raiz quadrada do total de Solar Coins da run — incentiva runs "completas" sem punir demais quem ascende cedo pra testar a loja meta.
- Tetos (caps) nos upgrades percentuais da Loja de Ascensão Geral evitam que, em runs muito avançadas (k alto), o jogo vire trivial — mantém a necessidade de gerenciar planetas mesmo em runs avançadas.
- Todos os valores numéricos (`C`, `crescimentoGeral`, `requisitoBaseEstabilidade`, tetos percentuais) ficam marcados como **pendentes de calibragem via playtest** — este documento define a fórmula/estrutura, não os números finais.

## 8. Perguntas em Aberto

1. A Loja de Ascensão Geral deve ter **custo crescente por nível** dentro de cada upgrade (ex: cada ponto no "Brilho Estelar" custa mais Pontos Estelares que o anterior), ou cada nó tem custo fixo e você simplesmente investe pontos linearmente?
2. Você quer que eu já pense em uma "Ascensão Geral 2" (segunda camada de prestígio, tipo NG+ do NG+) ou por ora o escopo é só uma camada de prestígio (o que já está definido aqui)?

---
**Próximo documento:** `05-arquitetura-tecnica.md` — agora entramos na parte técnica: stack Java, bibliotecas, padrões de arquitetura, estrutura de pacotes/módulos, e o game loop (tick de rotação/translação, sistema de eventos de ciclo).
