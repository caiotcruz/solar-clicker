# Solar Clicker — Documento 8: Especificação de Sistemas Core

> Status: rascunho v0.1
> Depende de: `05-arquitetura-tecnica.md`, `07-bignumber.md`
> Objetivo: detalhar, em nível de comportamento/API, os sistemas centrais que a arquitetura (doc 5) só esboçou em estrutura de pacotes.

## 1. Sistema de Clique

**Fluxo:** input → `ClickHandler` (na camada `render`/`input`) → chama `domain.economy` → aplica produção → publica evento `ClickAppliedEvent` (pra UI mostrar número flutuante, som, etc.).

```java
public interface Clickable {
    BigNumber onClick();   // retorna o quanto foi gerado neste clique, já com todos os modificadores aplicados
}
```

- `Sun` e cada `Planet` implementam `Clickable`, mas com cálculo diferente (Sol: fórmula simples da Loja Geral; Vênus: fórmula com bônus de foco em clique, doc 3).
- Clique crítico (Vênus): resolvido dentro do `onClick()` do próprio planeta — um `Random` (seedado, ver observação de testabilidade abaixo) decide se o clique é crítico antes de aplicar o multiplicador.
- **Testabilidade:** qualquer uso de aleatoriedade (crítico de clique) deve receber o `Random` via injeção (construtor), nunca `new Random()` direto dentro do método — assim os testes unitários podem fixar a seed e testar os dois branches (crítico/não-crítico) de forma determinística.

## 2. Sistema de Ciclo Orbital (detalhamento do `OrbitalCycle`, doc 5)

```java
public class OrbitalCycle {
    private final double rotationDurationSeconds;
    private final double translationDurationSeconds;
    private double rotationProgress;      // 0.0 – 1.0
    private double translationProgress;   // 0.0 – 1.0

    public CycleResult advance(double deltaSeconds) {
        // avança progresso, calcula quantos ciclos completos ocorreram,
        // "sobra" (progresso residual) continua acumulando corretamente
        // mesmo que deltaSeconds seja maior que a duração de um ciclo inteiro
        // (importante para o cálculo de progresso offline, seção 4)
    }
}
public record CycleResult(int rotationsCompleted, int translationsCompleted) {}
```

**Importante:** `advance()` precisa lidar corretamente com `deltaSeconds` muito grande (ex: cálculo de progresso offline de horas) sem loop — usar divisão inteira (`deltaSeconds / duration`) para calcular ciclos completos de uma vez, não incrementar frame a frame.

## 3. `PlanetFocus` (Strategy do foco de cada planeta)

```java
public interface PlanetFocus {
    BigNumber modifyClickProduction(BigNumber base, PlanetState state);
    BigNumber modifyRotationYield(BigNumber base, PlanetState state);
    BigNumber modifyTranslationYield(BigNumber base, PlanetState state);
    void onLocalAscension(PlanetState state); // aplica o efeito especial do foco (ex: Terra afeta produção idle global)
}
```

Cada foco (Mercúrio-Generalista, Vênus-Clique, Terra-Idle, Marte-Upgrades, Júpiter-Escala, Saturno-Automação, Urano-Eficiência, Netuno-Buff, Plutão-Ascensão) implementa essa interface conforme as regras do doc 3. Isso evita um `PlanetService` gigante cheio de `if (planet.getId().equals("venus"))`.

## 4. Cálculo de Progresso Offline

**Decisão confirmada:** progresso offline tem **limite de tempo** (cap) e **penalidade percentual** na produção enquanto offline. Ambos os valores são, por padrão, ajustáveis via Loja de Ascensão Geral (doc 4) — o que cria naturalmente uma "build focada em offline": um jogador pode, ao longo de várias Ascensões Gerais, investir pesado nos nós que aumentam o cap e reduzem a penalidade, viabilizando um estilo de jogo mais desatento/AFK do que o padrão.

```java
public class OfflineProgressCalculator {
    public OfflineProgressResult calculate(SaveData save, long secondsSinceLastSave,
                                            long offlineCapSeconds, double offlinePenaltyMultiplier) {
        long effectiveSeconds = Math.min(secondsSinceLastSave, offlineCapSeconds);
        // para cada planeta desbloqueado:
        //   CycleResult = OrbitalCycle.advance(effectiveSeconds)
        //   moeda ganha = colheita(CycleResult) * offlinePenaltyMultiplier
        // retorna um resumo (pra UI mostrar "Enquanto você esteve fora: +X Solar Coins")
    }
}
```

- `offlineCapSeconds`: valor base pequeno (ex: poucas horas, calibragem em playtest — doc 10), aumentável permanentemente pelo nó **"Sono Estelar"** da Loja de Ascensão Geral (doc 4).
- `offlinePenaltyMultiplier`: valor base parcial (ex: fração da produção online, calibragem em playtest), aproximável de 100% pelo nó **"Vigília Eficiente"** da Loja de Ascensão Geral (doc 4).
- Roda **uma vez**, na inicialização do jogo, antes da primeira tela ser desenhada. Resultado é acumulado e exibido como um resumo simples ("bem-vindo de volta") — não re-simulamos visualmente cada rotação perdida, só aplicamos o resultado matemático.
- Automação de Saturno (doc 3, seção 7 — "auto-coleta reduz perda quando offline") funciona como uma camada **adicional e local** a esse sistema global: Saturno desbloqueado + upgradado reduz ainda mais a perda especificamente para si mesmo, empilhando com os nós gerais da Ascensão.

## 5. Loja Geral (`GeneralShop`)

- Contém upgrades que afetam o Sol e sistemas globais (não específicos de um planeta) — ex: poder de clique base do Sol, slots de automação inicial (se comprados fora da Ascensão Geral), etc.
- Mesma `CostFormula` genérica do doc 2 (`custoBase * crescimento^nível`).
- Estruturalmente é só uma lista de `UpgradeDefinition` (mesma classe usada nas árvores locais dos planetas — ver seção 6) com `scope = GLOBAL`.

## 6. Árvores de Upgrade (estrutura genérica)

Todos os upgrades do jogo (Loja Geral, upgrades locais de planeta, Loja de Ascensão Geral) compartilham a mesma estrutura de dados, mudando só configuração:

```java
public class UpgradeDefinition {
    String id;
    String scope;              // "global" | "planet:<id>" | "ascension"
    BigNumber baseCost;
    double costGrowthRate;
    UpgradeEffect effect;      // o que o upgrade faz (produção, redução de custo, etc.)
    int maxLevel;              // -1 = sem limite
}
```

- `UpgradeEffect` é outra interface pequena (Strategy) — permite compor efeitos sem precisar de uma classe nova por upgrade individual na maioria dos casos (ex: "multiplicador percentual de produção" é o mesmo `UpgradeEffect` reaproveitado por dezenas de upgrades diferentes, só mudando o parâmetro).
- Dessa forma, adicionar um novo upgrade no jogo é, na maioria dos casos, **dado de configuração**, não código novo.

## 7. Decisão Confirmada

Progresso offline usa **cap de tempo + penalidade percentual** (detalhado na seção 4), com ambos os parâmetros evoluindo via Loja de Ascensão Geral — ver doc 4, seção 6 (nós "Sono Estelar" e "Vigília Eficiente").

---
**Próximo documento:** `09-ui-ux-e-roadmap-implementacao.md` — telas, HUD, opção de notação configurável (confirmada no doc 7), e o roadmap de fases de implementação (MVP → incrementos).
