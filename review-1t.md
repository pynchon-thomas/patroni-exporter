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