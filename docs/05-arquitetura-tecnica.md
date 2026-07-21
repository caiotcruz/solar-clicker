# Solar Clicker — Documento 5: Arquitetura Técnica

> Status: rascunho v0.1
> Depende de: todos os documentos de design (01–04)
> Decisões assumidas nesta versão (ajustar se necessário — ver seção 9):
> - Loja de Ascensão Geral: custo crescente por nível (mesmo padrão exponencial do resto do jogo).
> - Escopo: uma única camada de prestígio por enquanto.

## 1. Escolha de Stack

**Recomendação: Java + LibGDX** (em vez de Swing/AWT puro ou JavaFX puro).

Justificativa:
- Você definiu "Java puro com bibliotecas" — LibGDX é uma biblioteca (não uma engine visual tipo Unity/Godot com editor próprio), então continua sendo "escrever o jogo em código", só que com rendering, input, câmera, texturas e áudio já resolvidos.
- Suporte nativo a **câmera 2D com pan** (exatamente o que você precisa pro movimento WASD lateral para ver os planetas).
- Suporte a **texture atlas** e animação de sprites — direto para os pixel arts que você já tem prontos.
- Roda em desktop (LWJGL) sem complicação, e mantém a porta aberta pra Android no futuro, se um dia você quiser (idle games costumam ir bem em mobile).

**Alternativas descartadas (e por quê):**
- *Swing/AWT puro:* funciona, mas você reimplementaria câmera, texture atlas e animação manualmente — trabalho evitável.
- *JavaFX puro:* melhor que Swing para 2D, mas ainda exige montar câmera/pan e pipeline de sprite na mão; LibGDX já entrega isso pronto e é o padrão de facto pra jogos 2D em Java.

⚠️ Se você já tinha outra biblioteca em mente na tentativa anterior do projeto, me avisa e eu adapto este documento — LibGDX é minha recomendação, não uma imposição.

## 2. Princípio Arquitetural Central: Separar Regras de Jogo da Renderização

O maior risco em jogos idle é a lógica econômica (que é **pura matemática/estado**) ficar emaranhada com código de desenho na tela. Vamos evitar isso com uma separação clara em camadas:

```
┌─────────────────────────────────────────┐
│  Presentation (LibGDX)                   │  ← desenha sprites, lê input WASD,
│  render/, input/, ui/                    │    anima câmera
├─────────────────────────────────────────┤
│  Application (orquestração)              │  ← game loop, tick, disparo de eventos
│  engine/                                 │
├─────────────────────────────────────────┤
│  Domain (regras de jogo — puro Java,     │  ← economia, planetas, ascensão,
│  SEM dependência de LibGDX)               │    BigNumber. 100% testável sem abrir janela.
│  economy/, planets/, ascension/, core/   │
├─────────────────────────────────────────┤
│  Persistence                             │  ← save/load, serialização
│  save/                                   │
└─────────────────────────────────────────┘
```

**Regra de ouro:** o pacote `domain` nunca importa nada de `com.badlogic.gdx.*`. Isso permite, por exemplo, testar toda a economia do jogo com JUnit puro, sem precisar abrir uma janela gráfica — importantíssimo pra um jogo cheio de fórmula e balanceamento como este.

## 3. Estrutura de Pacotes Proposta

```
com.solarclicker
├── domain
│   ├── core
│   │   └── BigNumber.java            // classe de número grande (doc 7)
│   ├── economy
│   │   ├── Currency.java             // Solar Coins / moeda local genérica
│   │   ├── ModifierRegistry.java     // Registro Global de Modificadores (doc 2, sec 5)
│   │   └── CostFormula.java          // fórmulas de custo/produção (doc 2)
│   ├── planets
│   │   ├── Planet.java               // entidade planeta (estado)
│   │   ├── PlanetFocus.java           // enum/strategy do foco (Clique/Idle/Upgrade/...)
│   │   ├── OrbitalCycle.java          // rotação/translação, progresso, eventos de ciclo
│   │   └── PlanetConfig.java          // dados fixos por planeta (custoBase, fatorBonus, etc.)
│   └── ascension
│       ├── LocalAscension.java
│       └── GeneralAscension.java
├── engine
│   ├── GameLoop.java                  // tick fixo, delta accumulation
│   ├── EventBus.java                  // eventos (ciclo completo, ascensão, desbloqueio)
│   └── OfflineProgressCalculator.java // cálculo de progresso "enquanto o jogo estava fechado"
├── render (LibGDX)
│   ├── SolarSystemScreen.java
│   ├── PlanetSprite.java
│   └── CameraController.java          // WASD pan
├── ui (LibGDX Scene2D)
│   ├── ShopUI.java
│   ├── PlanetPanelUI.java
│   └── AscensionUI.java
├── input
│   └── CameraInputHandler.java
└── save
    ├── SaveData.java                  // DTO serializável (doc 6)
    ├── SaveManager.java
    └── SaveMigration.java             // versionamento de save
```

## 4. Game Loop e Sistema de Ciclos (Rotação/Translação)

Como a economia agora paga **por ciclo completo** (doc 2, seção 7), o game loop precisa:

1. Rodar em **tick de tempo real** (delta time acumulado, padrão LibGDX `render(float delta)`), não em frames — pra rotação/translação não dependerem de FPS.
2. Cada `Planet` tem um `OrbitalCycle` com:
   - `progressoRotacao` (0.0 a 1.0)
   - `progressoTranslacao` (0.0 a 1.0)
   - Ao progresso bater 1.0 → dispara evento (`RotationCompletedEvent` / `TranslationCompletedEvent`) via `EventBus` → reseta progresso pra 0 → aplica `colheitaRotacao`/`colheitaTranslacao` na moeda local.
3. **Progresso offline:** quando o jogo é reaberto, `OfflineProgressCalculator` recebe o timestamp do último save e simula quantos ciclos completos aconteceriam nesse intervalo (importante: planetas rápidos como Mercúrio podem completar centenas de rotações offline — o cálculo precisa ser em lote, não em loop de milissegundo por milissegundo, por performance).

```java
// Pseudocódigo simplificado do tick
void tick(float deltaSeconds) {
    for (Planet p : planets) {
        if (!p.isUnlocked()) continue;
        CycleResult result = p.getOrbitalCycle().advance(deltaSeconds);
        for (int i = 0; i < result.rotationsCompleted; i++) {
            eventBus.publish(new RotationCompletedEvent(p));
        }
        for (int i = 0; i < result.translationsCompleted; i++) {
            eventBus.publish(new TranslationCompletedEvent(p));
        }
    }
}
```

## 5. Padrões de Projeto Recomendados

| Padrão | Onde | Por quê |
|---|---|---|
| **Observer / Event Bus** | Ciclos completos, ascensões, desbloqueios | Desacopla economia de UI/áudio/animação — quando a Terra completa uma translação, quem quiser reagir (UI, som, partícula) só escuta o evento. |
| **Strategy** | `PlanetFocus` | Cada foco (Clique/Idle/Upgrade/Escala/Automação/Eficiência/Buff/Ascensão-prep) implementa a mesma interface, mas calcula produção/efeitos diferente. |
| **Factory** | Criação de `Planet` a partir de `PlanetConfig` | Planetas são "dados" (doc 3) instanciados a partir de configuração, não hardcoded em `if/else`. |
| **DTO + Migration** | Save/Load | Separar o "modelo de domínio rico" do "formato salvo em disco", permitindo migrar saves antigos quando o jogo evoluir (ver doc 6). |

**Evitar:** Singleton global para tudo (ex: um `GameManager` estático gigante). Prefira injeção simples via construtor — facilita testes unitários da camada `domain`.

## 6. Requisitos Não-Funcionais

- **Plataforma:** desktop (Windows/Linux/Mac via LWJGL), execução via `.jar` empacotado.
- **Performance:** carga leve — poucas dezenas de entidades (planetas) e nenhum sistema de física pesado. O ponto de atenção real é o `OfflineProgressCalculator` (precisa ser O(1) por planeta, calculado por fórmula, não por simulação passo-a-passo de tempo offline).
- **Persistência:** save local em arquivo (JSON, ver doc 6), com autosave periódico (ex: a cada X segundos ou a cada evento de ciclo importante).
- **Números:** toda a camada `domain` deve operar exclusivamente com `BigNumber` (doc 7) para valores de moeda/produção — nunca `double` cru, para evitar perda de precisão em runs longas.

## 7. Testabilidade

Como a camada `domain` não depende de LibGDX, a meta é ter testes unitários (JUnit) cobrindo:
- Fórmulas de custo/produção (doc 2)
- `ModifierRegistry` (interdependência entre planetas)
- Ciclos de rotação/translação e disparo de eventos
- Ascensão local e geral (incluindo cálculo de Pontos Estelares)
- `BigNumber` (aritmética, comparação, formatação)

Isso é especialmente importante num jogo de números grandes: bug de fórmula é silencioso (não trava o jogo, só deixa a economia quebrada) — testes automatizados pegam isso antes de chegar no jogador.

## 8. Diagrama de Fluxo de Alto Nível

```
[Input WASD] → CameraInputHandler → CameraController (pan da view)
[Clique do mouse no Sol/Planeta] → render/ → domain.economy (aplica produção de clique)
[GameLoop.tick] → domain.planets.OrbitalCycle.advance() → EventBus → 
    ├── domain.economy (aplica colheita) 
    ├── render (dispara animação de "pagamento")
    └── save.SaveManager (marca estado sujo para autosave)
[UI de compra] → domain.economy.CostFormula → valida e aplica upgrade
[Botão Ascensão Geral] → domain.ascension.GeneralAscension → reseta domain, preserva Pontos Estelares
```

## 9. Perguntas em Aberto

1. Confirma **LibGDX** como biblioteca de renderização, ou você já tinha outra em mente da tentativa anterior do projeto (Swing? JavaFX? outra)?
2. Autosave: prefere por **intervalo de tempo fixo** (ex: a cada 30s) ou por **evento** (a cada vez que algo relevante muda, com um debounce)? Ou os dois?
3. Você pretende rodar isso só localmente (jar) ou já pensa em algum tipo de distribuição (ex: itch.io)? Isso não muda a arquitetura agora, mas pode influenciar decisões de empacotamento mais pra frente.

---
**Próximo documento:** `06-modelo-de-dados.md` — schema do save (JSON), versionamento e estratégia de migração.
