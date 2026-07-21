# Solar Clicker — Documento 7: Classe `BigNumber`

> Status: rascunho v0.1
> Depende de: `02-economia-balanceamento.md`, `04-sistema-ascensao.md`, `06-modelo-de-dados.md`

## 1. Objetivo

Representar valores monetários (Solar Coins, moedas locais, Pontos Estelares) que crescem muito além do limite prático de um `double` (~1.7 × 10^308, e com perda de precisão bem antes disso). O `BigNumber` precisa:
- Suportar valores absurdamente grandes sem overflow.
- Ter operações rápidas (jogo faz milhares de cálculos por segundo entre 9 planetas).
- Ser **imutável** (cada operação retorna um novo `BigNumber`, nunca modifica os operandos — evita bugs sutis de estado compartilhado).
- Serializar de forma legível (ver doc 6).

## 2. Representação Interna

**Notação científica normalizada:** mantissa (`double`) + expoente (`long`).

```java
public final class BigNumber {
    private final double mantissa;  // sempre em [1.0, 10.0) exceto quando o valor é zero
    private final long exponent;
    ...
}
```

**Invariante de normalização:** após qualquer operação, o resultado é normalizado para que `1.0 <= mantissa < 10.0` (exceto o caso especial de zero, representado como `mantissa = 0.0, exponent = 0`).

Por que `long` no expoente (e não `int`): mesmo que `int` já suporte expoentes de até ~2 bilhões (muito mais que qualquer idle game precisa), `long` custa a mesma complexidade de código e elimina qualquer preocupação de overflow do próprio expoente em runs extremamente longas ou builds futuras do jogo (ex: uma expansão pós-lançamento com mais planetas/prestígios).

## 3. Faixa e Precisão

- **Faixa prática:** de `10^-323` (menor double normal) até, na prática, qualquer expoente que caiba em `long` — ou seja, sem limite relevante para o jogo.
- **Precisão:** a mantissa como `double` garante ~15-17 dígitos decimais de precisão relativa. Isso é mais que suficiente para um jogo idle (ninguém percebe diferença no 16º dígito de uma moeda) — não precisamos de `BigDecimal`/`BigInteger` (mais lento e sem necessidade aqui, já que não exibimos nem precisamos de precisão exata arbitrária, só escala).

## 4. Operações Suportadas

| Operação | Assinatura | Observação |
|---|---|---|
| Soma | `BigNumber add(BigNumber other)` | Alinha expoentes antes de somar mantissas (padrão de soma em notação científica) |
| Subtração | `BigNumber subtract(BigNumber other)` | Trunca em zero se resultado for negativo (moeda de jogo não fica negativa — ver seção 7) |
| Multiplicação | `BigNumber multiply(BigNumber other)` | Soma expoentes, multiplica mantissas, renormaliza |
| Multiplicação por escalar | `BigNumber multiply(double scalar)` | Atalho comum (ex: aplicar um multiplicador percentual) |
| Divisão | `BigNumber divide(BigNumber other)` | Erro/exceção customizada se divisor for zero |
| Potência inteira | `BigNumber pow(long exponentInt)` | Usada nas fórmulas de custo (`custoBase * crescimento^n`) |
| Potência fracionária | `BigNumber pow(double exponentFraction)` | Usada na fórmula de Pontos Estelares (raiz quadrada) e bônus de ascensão local (`^0.8`) — ver seção 5 |
| Raiz quadrada | `BigNumber sqrt()` | Atalho para `pow(0.5)` |
| Comparação | `int compareTo(BigNumber other)` | Implementa `Comparable<BigNumber>` |
| Mínimo/Máximo | `static BigNumber min(...)`, `static BigNumber max(...)` | |
| Piso | `BigNumber floor()` | Usada na fórmula de Pontos Estelares (`piso(...)`, doc 4) |
| Consultas | `boolean isZero()`, `boolean isNegative()` | |
| Conversão | `double toDouble()` (com perda de precisão avisada em Javadoc), `String toPlainString()` (debug) | `toDouble()` satura em `Double.MAX_VALUE`/`POSITIVE_INFINITY` se o valor for grande demais para caber — nunca deve ser usado para lógica de jogo, só debug/telemetria |

## 5. Potências Fracionárias — Técnica de Implementação

Elevar `mantissa × 10^expoente` a uma potência fracionária `p` não pode ser feito ingenuamente (mantissa^p estoura ou perde sentido). A técnica padrão (usada por bibliotecas como break_eternity.js, adaptada aqui para Java):

```
log10(valor) = log10(mantissa) + expoente
valor^p      = 10 ^ (p * log10(valor))
```

Depois, o resultado `10^(novoExpoenteTotal)` é renormalizado separando parte inteira e fracionária do novo expoente:
```java
double totalExponent = exponentFraction * (Math.log10(mantissa) + exponent);
long newExponent = (long) Math.floor(totalExponent);
double newMantissa = Math.pow(10, totalExponent - newExponent);
```
Isso mantém a operação estável mesmo para expoentes fracionários em valores com expoente `long` gigante.

## 6. Serialização

Consistente com o doc 6: formato de string `"mantissa:expoente"`, por exemplo `"1.234:15"` para `1.234 × 10^15`. Zero é serializado como `"0:0"`.

```java
public String toSaveString() {
    return mantissa + ":" + exponent;
}
public static BigNumber fromSaveString(String s) { ... }
```

## 7. Formatação para Exibição (Notação Abreviada)

A formatação é **desacoplada** do `BigNumber` em si — o `BigNumber` só sabe fazer conta; quem decide como mostrar na tela é uma classe separada `BigNumberFormatter` (Strategy), para permitir trocar o estilo de notação sem mexer na lógica matemática.

**Estilo padrão proposto** (sufixos nomeados até um ponto, depois notação científica):

| Faixa | Exibição |
|---|---|
| < 1.000 | número cheio (ex: `842`) |
| 10^3 – 10^5 | `K` (ex: `1.23K`) |
| 10^6 – 10^8 | `M` |
| 10^9 – 10^11 | `B` |
| 10^12 – 10^14 | `T` |
| 10^15 – 10^17 | `Qa` (Quadrilhão) |
| 10^18 – 10^20 | `Qi` (Quintilhão) |
| 10^21 – 10^23 | `Sx` (Sextilhão) |
| 10^24 – 10^26 | `Sp` (Septilhão) |
| 10^27 – 10^29 | `Oc` (Octilhão) |
| 10^30 – 10^32 | `No` (Nonilhão) |
| 10^33 – 10^35 | `Dc` (Decilhão) |
| ≥ 10^36 | notação científica pura (ex: `1.23e36`) |

Regras de exibição:
- 2 casas decimais quando abaixo de 100 na mantissa (ex: `1.23K`), 1 casa decimal quando a mantissa já tem 2 dígitos inteiros (ex: `12.3K`), sem casas decimais com 3 dígitos inteiros (ex: `123K`) — padrão comum em idle games para manter o texto sempre com o mesmo número de caracteres (evita "pulos" de layout na UI).
- Formatação é 100% baseada no valor de `BigNumber`, nunca em `double` convertido (evitar o bug clássico de mostrar um número "estranho" por erro de conversão).

## 8. Casos Extremos e Regras

- **Moeda nunca fica negativa:** `subtract` que resultaria em negativo é truncado em zero (`BigNumber.ZERO`) — decisão de design pra simplificar toda a camada de economia (nenhum sistema do jogo precisa representar dívida).
- **Zero é um caso especial** na normalização (mantissa/expoente ambos zero), tratado explicitamente em toda operação para não gerar `log10(0) = -Infinity` na técnica da seção 5.
- **Comparação com `double`/`long` "normais":** métodos de conveniência `static BigNumber of(double value)` e `static BigNumber of(long value)` para criar um `BigNumber` a partir de literais simples (ex: custo base de um upgrade definido em config), normalizando na criação.

## 9. Perguntas em Aberto

1. Notação nomeada (Qa, Qi, Sx...) até Decilhão e depois notação científica — está bom, ou você prefere ir direto para notação científica mais cedo (ex: já a partir de Trilhão), ou manter os nomes por mais tempo (sistema de duas letras tipo `aa`, `ab`, `ac`... comum em alguns clickers)?
2. Quer que eu inclua no doc de UI, mais pra frente, uma opção de configuração pro jogador escolher o estilo de notação (algo que jogadores hardcore de idle costumam gostar)?

---
**Próximo documento:** `08-especificacao-sistemas-core.md` — detalhamento dos demais sistemas centrais (Save/Load em código, Loja Geral, Sistema de Clique, árvores de upgrade) complementando o que já foi definido na arquitetura (doc 5).
