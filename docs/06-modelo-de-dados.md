# Solar Clicker — Documento 6: Modelo de Dados (Save)

> Status: rascunho v0.1
> Depende de: `05-arquitetura-tecnica.md`
> Decisão confirmada: alvo de distribuição é a **Steam**. Isso impacta save (Steam Cloud) e empacotamento — anotado ao longo do documento.

## 1. Impacto da Steam no Modelo de Dados

- **Steam Cloud** sincroniza arquivos por caminho local — nosso `SaveManager` deve gravar em um caminho padrão de "app data" do usuário (não em `Program Files` nem relativo ao `.jar`), pra funcionar bem com o Cloud.
- **Escrita atômica é obrigatória:** se o Steam sincronizar/fechar o processo no meio de uma escrita, um save corrompido é o pior tipo de bug pra esse gênero (jogador perde horas/dias de progresso idle). Estratégia: escrever em arquivo temporário → renomear por cima do save real (rename é atômico na maioria dos sistemas de arquivo).
- **Conquistas/Estatísticas Steam:** vou incluir no schema um bloco de `statistics` vitalício (sobrevive a Ascensões Gerais) pensado pra alimentar conquistas futuras (ex: "chegou a Plutão pela primeira vez", "fez 10 Ascensões Gerais").
- **Biblioteca sugerida para integração Steam:** `steamworks4j` (binding Java para a Steamworks SDK) — permite achievements, cloud save e stats sem sair de Java. Fica marcado como dependência a adicionar quando chegarmos na fase de empacotamento (fora do escopo deste doc, vai para o roadmap de implementação, doc 9).

## 2. Formato Geral do Arquivo

- Formato: **JSON**, legível, versionado.
- Nome de campo `schemaVersion` no topo — toda leitura de save passa primeiro por esse número antes de qualquer outra coisa.
- Serialização de `BigNumber` (doc 7) **não** usa `double` — usa uma string no formato `"mantissa:expoente"`, por exemplo `"1.234:15"` representa `1.234 × 10^15`. Isso evita perda de precisão e mantém o arquivo legível/editável manualmente (útil em debug).

## 3. Schema Completo (v1)

```json
{
  "schemaVersion": 1,
  "savedAtEpochMillis": 1737300000000,

  "solarCoins": "3.421:8",

  "planets": [
    {
      "id": "mercury",
      "unlocked": true,
      "localCurrency": "9.02:5",
      "upgradeLevels": {
        "click_power": 12,
        "rotation_speed": 4,
        "rotation_yield": 7
      },
      "orbitalCycle": {
        "rotationProgress": 0.42,
        "translationProgress": 0.08
      },
      "localAscensionLevel": 3
    },
    {
      "id": "pluto",
      "unlocked": false,
      "localCurrency": "0:0",
      "upgradeLevels": {},
      "orbitalCycle": { "rotationProgress": 0.0, "translationProgress": 0.0 },
      "localAscensionLevel": 0,
      "orbitalStability": "0:0"
    }
  ],

  "generalAscension": {
    "ascensionCount": 2,
    "starPoints": "1.5:4",
    "shopLevels": {
      "stellar_shine": 5,
      "efficient_orbits": 2,
      "ancestral_engineering": 0,
      "inherited_stability": 1,
      "primordial_automation": 0,
      "accelerated_cycles": 3
    }
  },

  "statistics": {
    "lifetimeSolarCoinsEarned": "2.1:12",
    "totalAscensions": 2,
    "totalPlaytimeSeconds": 154302,
    "firstReachedPluto": true
  },

  "settings": {
    "masterVolume": 0.8,
    "cameraLastX": 340.0
  }
}
```

Notas sobre os campos:
- `orbitalStability` só existe no objeto de Plutão (campo específico daquele planeta — ver doc 3, seção 10). Planetas comuns não têm esse campo.
- `upgradeLevels` e `shopLevels` são mapas abertos (`id do upgrade → nível`), o que permite adicionar novos upgrades no futuro sem quebrar o schema — saves antigos simplesmente não têm a chave nova, e o código trata ausência de chave como nível 0.
- `orbitalCycle` guarda só o **progresso fracionário** (0.0–1.0), não o tempo absoluto — assim, se você rebalancear o tempo de rotação/translação de um planeta numa atualização futura, o progresso do jogador continua fazendo sentido proporcionalmente.

## 4. Estratégia de Versionamento e Migração

Cada mudança estrutural no save incrementa `schemaVersion`. O `SaveMigration` aplica uma cadeia de transformações, uma versão de cada vez:

```
loadRawJson()
  → if schemaVersion == 1: migrateV1toV2(json)
  → if schemaVersion == 2: migrateV2toV3(json)
  → ...
  → parse para SaveData (versão atual)
```

Regra: cada função de migração só sabe ir de `vN` para `vN+1` — nunca pula versões. Isso mantém cada migração pequena e testável isoladamente (e com playtime real acumulado, testes de migração são tão importantes quanto testes de economia).

## 5. Integridade de Escrita (detalhe da seção 1)

```
1. Serializar SaveData atual para JSON em memória
2. Escrever em "save.json.tmp"
3. Fechar o arquivo (flush garantido)
4. Renomear "save.json.tmp" → "save.json" (sobrescrevendo o anterior)
```
Se o processo morrer entre os passos 2 e 4, o `save.json` antigo permanece intacto — o jogador perde, no pior caso, o intervalo desde o último autosave (definido como intervalo fixo, conforme sua decisão no doc 5), nunca o save inteiro.

## 6. Perguntas em Aberto

1. Vamos manter **um único slot de save** (padrão pra idle games, e mais simples de sincronizar com Steam Cloud), ou você quer múltiplos slots desde já?
2. Tem alguma conquista específica em mente pra Steam, ou deixamos isso pra quando o jogo estiver mais avançado (o campo `statistics` já foi projetado para acomodar isso depois, sem quebrar o save)?

---
**Próximo documento:** `07-bignumber.md` — especificação completa da classe `BigNumber`: representação interna, operações suportadas, e as regras de formatação/notação abreviada (K, M, B...).
