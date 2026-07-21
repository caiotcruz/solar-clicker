# Solar Clicker — Documento 2: Economia & Balanceamento

> Status: rascunho v0.1
> Depende de: `01-GDD-visao-geral.md`
> Premissas assumidas nesta versão (ajustar se necessário):
> - 9 planetas: Mercúrio, Vênus, Terra, Marte, Júpiter, Saturno, Urano, Netuno, Plutão (sem Lua/cinturão de asteroides por ora).
> - Sem referências diretas de jogos específicas — segui padrões genéricos de mercado idle/incremental.

## 1. Moedas do Jogo

| Moeda | Escopo | Gerada por | Gasta em |
|---|---|---|---|
| **Solar Coins** | Geral | Clique no Sol + % de exportação das moedas locais (ver seção 4) | Loja geral, desbloqueio de planetas |
| Moeda de Mercúrio | Local | Clique/idle em Mercúrio | Upgrades locais de Mercúrio |
| Moeda de Vênus | Local | Clique/idle em Vênus | Upgrades locais de Vênus |
| Moeda da Terra | Local | Idle da Terra | Upgrades locais da Terra |
| Moeda de Marte | Local | Idle/upgrades de Marte | Upgrades locais de Marte |
| Moeda de Júpiter | Local | (foco a definir) | Upgrades locais de Júpiter |
| Moeda de Saturno | Local | (foco a definir) | Upgrades locais de Saturno |
| Moeda de Urano | Local | (foco a definir) | Upgrades locais de Urano |
| Moeda de Netuno | Local | Idle | Upgrades de buff para outros planetas |
| Moeda de Plutão | Local | Idle | Upgrades de preparação para Ascensão Geral |

*Nomes das moedas locais (ex: "Moedas Marcianas") ficam para o doc de UI/Copy — aqui tratamos só da função econômica.*

Ainda não defini o foco de Júpiter, Saturno e Urano — no GDD você mencionou só Mercúrio (geral fraco), Vênus (clique), Terra (idle), Marte (upgrade), Netuno (buff), Plutão (ascensão prep). Faltam 3 planetas com foco. Sugestão temporária até você decidir:
- **Júpiter:** multiplicador de escala (upgrades que dão saltos grandes, não incrementais)
- **Saturno:** automação (compra automática de upgrades baratos)
- **Urano:** eficiência (reduz custo de upgrades de outros planetas, em vez de aumentar produção)

## 2. Tempos de Rotação e Translação

Regra: Terra = referência (rotação 10s / translação 60min de jogo). Os demais são proporcionais aos períodos reais (dia sideral / ano) da Terra.

Fator de rotação: `10s por dia terrestre real`
Fator de translação: `3600s (60min) por ano terrestre real`

| Planeta | Rotação real (dias) | Rotação no jogo | Translação real (anos) | Translação no jogo |
|---|---|---|---|---|
| Mercúrio | 58,6 | ~9,8 min | 0,241 | ~14,5 min |
| Vênus | 243 (retrógrado) | ~40,5 min | 0,615 | ~36,9 min |
| Terra | 1 | 10 s | 1 | 60 min |
| Marte | 1,03 | ~10,3 s | 1,881 | ~1,88 h |
| Júpiter | 0,41 | ~4,1 s | 11,86 | ~11,86 h |
| Saturno | 0,44 | ~4,4 s | 29,46 | ~29,46 h |
| Urano | 0,72 (retrógrado) | ~7,2 s | 84,01 | ~3,5 dias |
| Netuno | 0,67 | ~6,7 s | 164,8 | ~6,87 dias |
| Plutão | 6,39 | ~63,9 s | 247,9 | ~10,33 dias |

⚠️ **Ponto de decisão para você:** a translação de Plutão dá ~10 dias corridos de jogo real. Isso é aceitável num idle (planeta de fim de jogo, ninguém precisa *ver* a volta completa), mas quero confirmar: a translação tem *algum efeito em gameplay* (bônus ao completar uma volta) ou é **só visual**? Isso muda bastante a arquitetura do sistema de órbitas.

## 3. Fórmulas de Custo (padrão idle genérico)

**Custo de upgrade local (nível n → n+1):**
```
custo(n) = custoBase * taxaCrescimento^n
```
- `taxaCrescimento` sugerido: 1.07–1.12 para upgrades baratos e frequentes; 1.15–1.30 para upgrades de impacto alto e raro.

**Custo de desbloqueio de planeta:**
```
custoDesbloqueio(planeta) = custoBase(planeta) * fatorEscalaGlobal
```
- Cada planeta tem um `custoBase` fixo definido manualmente na tabela de balanceamento (não fórmula genérica — desbloqueio de planeta é um marco, não uma curva).
- `fatorEscalaGlobal` cresce com o número de ascensões gerais já feitas (assim uma segunda run desbloqueia planetas mais rápido, mas não de graça).

## 4. Fórmulas de Produção

**Produção por clique:**
```
producaoPorClique = poderBaseClique * (1 + somaBonusPercentuais) * multiplicadoresDeAscensao
```

**Produção idle (por segundo):**
```
producaoPorSegundo = producaoBase * nivelUpgrades * (1 + somaBonusPercentuais) * multiplicadoresDeAscensao
```

**Exportação de moeda local → Solar Coins:**
Todo planeta exporta uma fração da sua produção local para o pool geral de Solar Coins, para o jogador nunca ficar "preso" sem moeda geral:
```
solarCoinsPorSegundo += producaoLocalPlaneta * taxaExportacao(planeta)
```
- `taxaExportacao` sugerida: mais alta em planetas iniciais (Mercúrio, Vênus), decrescendo nos planetas avançados — assim o late-game depende mais de estratégia entre moedas locais do que de "clique geral".

## 5. Interdependência Entre Planetas (o ponto central do jogo)

Proposta de arquitetura econômica — **Registro Global de Modificadores**:

Cada planeta pode registrar modificadores que afetam:
1. **Si mesmo** (padrão — a maioria dos upgrades)
2. **Um planeta específico** (ex: um upgrade de Vênus que ajuda especificamente a Terra)
3. **Todos os planetas** (ex: Netuno — foco declarado em "melhorar outros")
4. **O pool de Solar Coins diretamente**

Cada modificador tem: `tipo` (produção / custo / velocidade de rotação-translação / taxa de exportação), `alvo` (self / planetaX / todos / solarCoins), `valor` (percentual ou flat) e `origem` (qual planeta/upgrade concedeu).

Isso permite exatamente o que você descreveu: Netuno não produz muito sozinho, mas os upgrades dele são multiplicadores aplicados sobre a produção de *todos* os outros — então build sem Netuno é estritamente pior em produção total, mas Netuno sozinho não faz nada sem os outros planetas rodando.

## 6. Implicações para o `BigNumber`

Com produção idle rodando 24/7 e 9 fontes de moeda compostas multiplicativamente, os números crescem rápido. O `BigNumber` (documento 7) precisa suportar, no mínimo:
- Notação abreviada configurável (K, M, B, T, depois AA, AB... ou notação científica, a decidir no doc técnico)
- Operações: soma, subtração, multiplicação, potência (para custo exponencial), comparação
- Precisão suficiente para evitar overflow de `double` do Java em runs longas (usar `BigDecimal` internamente ou uma representação mantissa+expoente própria — decisão para o doc técnico)

## 7. Decisões Confirmadas (v0.2)

1. Foco de Júpiter/Saturno/Urano: **confirmado** — escala / automação / eficiência.
2. Translação e rotação **têm efeito de gameplay direto**. Isso substitui o modelo simples de "moeda por segundo" da seção 4:

### 7.1 Modelo de Produção Revisado — Pagamento por Ciclo

Em vez de (ou além de) gerar moeda continuamente por segundo, cada planeta paga a moeda local em **eventos de ciclo**:

- **Rotação completa** → paga `colheitaRotacao(planeta)` em moeda local do planeta. É o "pagamento pequeno e frequente" (a cada poucos segundos/minutos, dependendo do planeta).
- **Translação completa** → paga `colheitaTranslacao(planeta)` em moeda local **e** uma quantidade de Solar Coins. É o "pagamento grande e raro" — representa um marco (um "ano" daquele planeta).

```
colheitaRotacao(planeta)     = baseRotacao(planeta) * multiplicadores
colheitaTranslacao(planeta)  = baseTranslacao(planeta) * multiplicadores   // ordem de grandeza bem maior que a rotação
```

Para o jogador não ficar olhando pra tela sem feedback entre um pagamento e outro, a UI mostra uma **barra de progresso** da posição orbital/rotacional atual (preenchendo até completar o ciclo — isso é puramente visual/UX, não gera moeda fracionada).

Isso torna a seção 4 (produção por segundo) válida apenas para **Solar Coins vindos de clique direto no Sol** e para eventuais upgrades futuros de "produção constante" — o padrão geral de moeda local passa a ser por ciclo.

3. Objetivo econômico entre planetas: **confirmado como único e simples** — juntar moeda (geral e/ou local, dependendo do custo de desbloqueio) para liberar o próximo planeta. Não haverá sub-objetivos econômicos extras entre planetas nesta fase.

---
**Próximo documento:** `03-especificacao-planetas.md` — tabela completa por planeta.
