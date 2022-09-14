Изменить тип результата createKV(String s)
=========================================

За Типы в языках со статической типзацией
------------------------------------------ 

Типы - это не только тип данных, в первую очередь это механизм комплятора проверить на семантические ошибки в программе

в частности - операции над типами данных

    тип цвет
        операции
            цвет = цвет + цвет
        пример
            оранживый = красный + желтый

    тип число
        оперции
            число = число + число
        пример
            3 = 2 + 1

    ? = цвет + число
    // хз какой тип - бесмыслица

ряд вещей/явлений не смешиваются по смыслу и не переходят из одного в другое - но в программировании с динамической типизацией (js, python) лего допустить

За под типы (NewType)
-----------------------------

Во многих языках есть тип String, который может содержать любой набор символов

Часто для представления имени/фамилии человека используют String - а это не уже не очень

- Имя обычно одно слово без пробелов и цифр или каких либо других спец символов (в русском)
- Кол-во имен в руском - число конечное
- При большом желании можно представить ввиде `enum`
- Регистр букв в имени не важен
- Имена в русском склоняются 

Вот этого всего нет в String

**В данном случае имя является подмножеством String**

Возможно захочеться сделать такое: `class Name extends String` - но это не правильно, по скольку унаследуются операции от String, которые не применимы к Name.

    public Name +( Name first, Name second )

- те есть операция + от двух строк, но каков результат + от двух имен ?
- такое же с операцией сравнение на больше/меньше

следует так поступить

```java
public enum Склонение {
    Именительный,
    Родительный
}

public interface Name {
    boolean equals(Name name)
    Name lead(Склонение склонение)
}

public class NameImpl implements Name {
    public/private Name( String name )
}

enum Names implements Name {
    Ivan("Ivan"),
    Boris("Boris"),
    ...
    ;
    Names(String baseName) {
        ...
    }
}
```

см NewType idiom/pattern
- [The Newtype Pattern in Rust](https://www.worthe-it.co.za/blog/2020-10-31-newtype-pattern-in-rust.html)

За структуры
-----------------------------

ФИО это структура Имя + Фамилия + Отчество

Можно представить так ее использование

```java
public String report(String lastName, String firstName, String middleName)
{
    ... 
}
```

В люгкую можно перепутать где что при использовании

    var a = "Ivan"
    var b = "Petrovich"
    var c = "Sidorov"
    report(a,b,c)

Можно сделать такое

```java
class FIO {
    String firstName
    String middleName
    String lastName
    FIO(String lastName, String firstName, String middleName) {
        ....
    }
}
```

Будет работать в таком случае

```java
FIO fio = db.findById( 10 );
report( fio );
```

но не в этом

```java
FIO fio = new FIO("Ivan", "Petrovich", "Sidorov");
report( fio );
```

компилятор не поймает

а вот это уже значитеьно уменьшит вероятность ошибки

```java
class FIO {
    FirstName firstName
    MiddleName middleName
    LastName lastName
    FIO(LastName lastName, FirstName firstName, MiddleName middleName) {
        ....
    }
}
```

Типизация createKV(String s)
------------------------------

s - по коду принимает следующие значения:

1. nodeUp
2. null_nodeUp
3. members
4. null_members
5. leader

имеет смысл создать 5 разных классов/типов: NodeUp, NullNodeUp, Members, NullMembers, Leader

### Пример для NodeUp

Есть вот такой код

```java
Map<String,String[][]> createKV(String s){
    Map<String,String[][]> map = new HashMap<>();
    String[] k;
    String[] v;
    String[][] m;
    switch (s){
        case "nodeUp":
            k = new String[3];
            k[0]="state";
            k[1]="node_name";
            k[2]="node";

            v = new String[3];

            m = new String[2][3];
            m[0]=k;
            m[1]=v;

            map.put(s,m);
            break;
        ...
    return map;
}
```

вот так, в первом приблежении

```java
public class NodeUp {
    private static final NO_VALUE = "out of service";
    public String state = NO_VALUE;
    public String node_name = NO_VALUE;
    public String node = NO_VALUE;
}
```

сейчас использование так

```java
public void checkNode() throws ExecutionException {
    String [] k = createKV("nodeUp").get("nodeUp")[0];
    String[] v = createKV("nodeUp").get("nodeUp")[1];

    Metrics nodeUp = new Metrics("patroni_node_up");

    try {

        JsonNode jsonNodeCluster = (new ObjectMapper()).readTree(getResp(request_cluster).body());
        for (JsonNode e : jsonNodeCluster.get("members")) {
            if (e.get("host").asText().equals(this.node.split("//")[1])) {
                v[0]=strQuote((e.get("state").asText())); // label for state
                v[1]=strQuote((e.get("name").asText())); // label for node_name
                v[2]=strQuote(node.split("//")[1]); // label for node
            }
        }
        nodeUp.setLabels(k,v);
        nodeUp.setValue("1");
        collector.add(nodeUp);
        App.myCache.loadingCache.invalidate("patroni_node_up");
        App.myCache.loadingCache.get("patroni_node_up");



    } catch (NullPointerException | JsonProcessingException e) {String[][] m;
        if(App.myCache.loadingCache.size() >0){

            m = App.myCache.loadingCache.get("patroni_node_up");
            nodeUp.setLabels(m[0],m[1]);nodeUp.setValue("0");collector.add(nodeUp);}
        else{m = createKV("null_nodeUp").get("null_nodeUp");nodeUp.setLabels(m[0],m[1]);nodeUp.setValue("0");
        collector.add(nodeUp);}

    }
}
```

а будет вот так

```java
public void checkNode() throws ExecutionException {
    Metrics nodeUp = new Metrics("patroni_node_up");
    var nodeUpData = new NodeUp();

    try {
        JsonNode jsonNodeCluster = (new ObjectMapper()).readTree(getResp(request_cluster).body());
        for (JsonNode e : jsonNodeCluster.get("members")) {
            if (e.get("host").asText().equals(this.node.split("//")[1])) {
                nodeUpData.state = strQuote((e.get("state").asText()));
                nodeUpData.node_name = strQuote((e.get("name").asText()));
                nodeUpData.node = strQuote(node.split("//")[1]);
            }
        }
        // nodeUp.setLabels(k,v);
        nodeUp.setLabels(
            new String[]{"state","node_name","node"}, 
            new String[]{ 
                nodeUpData.state, 
                nodeUpData.node_name, 
                nodeUpData.node
            }
        );
        nodeUp.setValue("1");
        collector.add(nodeUp);
        App.myCache.loadingCache.invalidate("patroni_node_up");
        App.myCache.loadingCache.get("patroni_node_up");



    } catch (NullPointerException | JsonProcessingException e) {String[][] m;
        if(App.myCache.loadingCache.size() >0){

            m = App.myCache.loadingCache.get("patroni_node_up");
            nodeUp.setLabels(m[0],m[1]);nodeUp.setValue("0");collector.add(nodeUp);}
        else{m = createKV("null_nodeUp").get("null_nodeUp");nodeUp.setLabels(m[0],m[1]);nodeUp.setValue("0");
        collector.add(nodeUp);}

    }
}
```

По идее у тебя должно исчезнуть `createKV`, а ам где используется
замениться на создание экзмпляра определенного класса NodeUp / Members / Leader 

После можно свести Metrics и упростить его, но это после этого