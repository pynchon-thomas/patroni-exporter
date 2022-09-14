PatrCollect.createKV(String s)
==================================

Несколько замечаний

1. метод имеет смысл пометить `private`
2. дать полное имя метода - `createKeyValue`
3. дать имя аргументу `s`
4. изменить тип результата

Метод имеет смысл пометить `private`
--------------------------------------

Любой код разделяется на публичную и приватную часть
, смысл - публичные части используют другие классы, которые не знают о внутренней реализации.

Кол-во публичных частей следует минимизировать - что бы не приходилось много переписывать, а переписывать придется.

```mermaid
flowchart TB

subgraph base_num[basic number]
    subgraph public_bn[public]
        direction LR

        zero["N zero()"]
        one["N one()"]
        inv["N inverse(N n)"]

    end

    subgraph inner_bn[private]
        direction LR

        zero_int["int zero()"]
        one_int["int one()"]
        inv_int["int inverse(int n)"]

        zero_double["double zero()"]
        one_double["double one()"]

        zero --> zero_int
        zero --> zero_double

        one --> one_int
        one --> one_double

        inv --> inv_int
    end
end

subgraph summator

    subgraph public_sum[public]
        direction LR

        add["N add( N a, N b )"]
    end

    subgraph inner_sum[private]
        direction LR

        add_int["int add( int a, int b )"]
        add_int --> public_bn
    end

end

subgraph compare
    subgraph cmp_pub[public]
        cmp_eq["boolean cmp(N a, N b)"]
        cmp_less["boolean less(N a, N b)"]
    end
end

subgraph multiplier

    subgraph public_mul[public]
        mul["N mul(N a, N b)"]
    end

    subgraph inner_mul[private]
    end

end

inner_mul --> cmp_pub
inner_mul --> public_sum

```