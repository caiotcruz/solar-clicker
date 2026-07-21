# Solar Clicker — GDD (Documento 1: Visão Geral)

> Status: rascunho v0.1
> Stack alvo: Java puro + bibliotecas (a definir em detalhe no doc de Arquitetura Técnica)

## 1. Resumo Executivo

Solar Clicker é um jogo Idle/Clicker ambientado no sistema solar. O jogador começa clicando no Sol para gerar **Solar Coins**, a moeda geral do jogo. Com o tempo, desbloqueia planetas em ordem (Mercúrio → Vênus → Terra → Marte → ... → Plutão), cada um com sua própria moeda local, seu próprio foco de gameplay e sua própria árvore de melhorias. O jogo termina cada "ciclo" com uma Ascensão Geral em Plutão, que reseta o progresso mas concede multiplicadores permanentes para a próxima run — o clássico loop de prestígio de jogos idle, aplicado à escala do sistema solar.

## 2. Pilares de Design

1. **Escala e proporção real como tema, não como fardo.** Usamos proporções reais (baseadas no dia/ano da Terra) para dar autenticidade, mas simplificamos a matemática para não travar o gameplay.
2. **Cada planeta é uma "build" diferente.** O jogador não joga um clicker genérico 8 vezes — cada planeta empurra para um estilo diferente (clique, idle, upgrades, suporte, prestígio).
3. **Interdependência das moedas.** Nenhuma moeda vive isolada; decisões em um planeta afetam a eficiência de outro (ex: Netuno buffando os demais). Isso cria uma camada de otimização/estratégia acima do clicker puro.
4. **Números grandes, mas legíveis.** Notação abreviada (K, M, B, ...) sempre visível, nunca "1.234.567.890.123" cru na tela.
5. **Progressão sempre visível.** O jogador deve sempre ter um próximo objetivo claro (próximo planeta, próximo upgrade, próxima ascensão).

## 3. Fantasia Central

"Você é a força que desperta o sistema solar." O sol acorda primeiro; cada clique é uma centelha de energia que, acumulada, permite reativar o próximo corpo celeste. Ascender é reiniciar o sistema solar num nível de energia mais alto — como um novo Big Bang particular daquele sistema.

## 4. Core Loop

```
Clicar no Sol / Planeta ativo
        ↓
Ganhar Solar Coins + Moeda Local do planeta
        ↓
Comprar upgrades (loja geral OU árvore local do planeta)
        ↓
Produção passiva (idle) aumenta
        ↓
Acumular o suficiente para desbloquear o próximo planeta
        ↓
Repetir com um planeta a mais rodando em paralelo
        ↓
(fim de ciclo) Ascensão local por planeta
        ↓
(fim do sistema) Ascensão Geral em Plutão → reset com multiplicadores permanentes
        ↓
Novo ciclo, mais rápido, mais fundo
```

## 5. Loop de Sessão vs. Loop de Meta-progressão

- **Curto prazo (segundos/minutos):** cliques, compra de upgrades baratos, observar rotação/translação dos planetas.
- **Médio prazo (uma sessão/dia):** desbloquear um novo planeta, montar a primeira árvore de upgrades dele.
- **Longo prazo (múltiplas sessões):** ascensão local de cada planeta, chegar a Plutão, Ascensão Geral.

## 6. Referências de Gênero

Idle/incremental games clássicos como referência de estrutura de progressão e curvas de custo (não copiamos conteúdo, só estudamos padrões de mercado):
- Jogos de clique com prestígio em camadas (reset parcial + reset total).
- Jogos com "múltiplos motores de produção" que interagem entre si.

*(Se você tiver 2-3 jogos específicos que quer usar como referência direta de sensação de progressão, me diz que eu incorporo nas notas de balanceamento.)*

## 7. Escopo do MVP

**Dentro do MVP:**
- Sol (clicável) + Solar Coins
- Mercúrio, Vênus, Terra desbloqueáveis (3 primeiros planetas, cobrindo focos "geral fraco", "click", "idle")
- Loja geral
- Classe BigNumber funcional
- Sistema de rotação/translação visual básico
- Save/Load simples

**Fora do MVP (fase 2+):**
- Marte em diante
- Árvores de ascensão completas
- Ascensão Geral em Plutão
- Polimento de UI/animações

## 8. Não-Objetivos

- Não é um jogo multiplayer.
- Não vamos simular física orbital real (é aproximação visual, não simulação astronômica).
- Não haverá monetização/IAP nesta fase de documentação (pode ser revisitado depois).

## 9. Público-Alvo

Jogadores de idle/incremental games que gostam de otimização e "quebra-cabeça de economia", com um verniz educativo leve (proporções reais do sistema solar).

---
**Próximo documento:** `02-economia-balanceamento.md` — vamos detalhar Solar Coins, moedas locais, fórmulas de custo/produção e a matemática de interdependência entre planetas.
