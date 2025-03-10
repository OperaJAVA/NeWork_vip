10.03.2025 / 20.02.2025
## Дипломный проект по профессии «Android Developer»

#### Мобильное приложение NeWork.

Описание

Приложение реализует следующие блоки пользовательского интерфейса:

#### Основной экран
Стартовый экран должен включать в себя следующие элементы:

Нижнее меню с тремя кнопками: «Посты», «События» и «Пользователи». Кнопку меню в
AppBar, которая позволяет перейти к входу в аккаунт, регистрации или просмотру профиля.

Экран со списком постов.

Этот фрагмент главного экрана включает в себя:

Список постов. Кнопку «+», нажатие на которую открывает возможность создать новый пост
или инициирует диалог с предложением войти или зарегистрироваться.
#### Карточка поста
Карточка поста должна содержать следующие элементы:

1. Аватар автора.

2. Имя автора.

3. Дата публикации в формате «день.месяц.год час:минута».

4. Кнопка «лайк» с указанием количества лайков.

5. Вложение (если есть): аудио, видео или фото.

6. Ссылка (если есть) для перехода в браузер.

7. Кнопка вызова меню с возможностью удаления или редактирования поста (если
   текущий пользователь является автором).

8. Текст поста.

#### Экран со списком событий
Этот фрагмент главного экрана включает:

Список событий. Кнопку «+», нажатие на которую открывает возможность создания нового
события или запускает диалог с предложением войти или зарегистрироваться.

Карточка события должна содержать:

Аватар автора. Имя автора.
Дату публикации в формате dd.mm.yyyy HH:mm. Дату проведения в формате dd.mm.yyyy HH:mm.
Тип события: офлайн или онлайн. Кнопку «лайка» с количеством лайков.
Вложение, если оно есть: аудио, видео или фото. Ссылку для перехода в браузер, если она есть.
Кнопку вызова меню с возможностью удаления или перехода к редактированию, если
текущий пользователь является автором. Текст поста.
#### Экран со списком пользователей
Этот фрагмент главного экрана представляет собой список пользователей.

Каждая карточка пользователя включает в себя:

* Логин;

* Имя;

* Аватар.

При нажатии на карточку пользователя происходит переход к более детальному
представлению информации.

#### Экран входа в приложение
Для входа в систему следует использовать поля ввода логина и пароля.

При этом должны быть соблюдены следующие основные требования:

* Логин — это непустая строка.

* Пароль — тоже не должен быть пустым.

Если введенные данные не соответствуют указанным ограничениям, в полях должны
появляться информативные сообщения с возможностью исправить ошибки.

Если сервер возвращает код 400, необходимо отобразить ошибку в виде сообщения
«Неправильный логин или пароль».

#### Экран регистрации
Для регистрации пользователей необходимо предусмотреть поля ввода для логина, имени,
пароля и его повтора. Также следует добавить кнопку, позволяющую выбрать фотографию и
увидеть её предварительный просмотр.

Важно, чтобы система проверяла основные ограничения на значения указанных полей:

* Логин — непустая строка;

* Имя — непустая строка;

* Пароль — непустая строка;

* Аватар — изображение в формате jpeg или png, максимальный размер 2048х2048.

Если введенные данные не соответствуют требованиям, в полях должны появляться
информативные сообщения с возможностью их исправления.

В случае получения от сервера кода ошибки 400, необходимо отобразить ошибку в виде
сообщения «Пользователь с таким логином уже зарегистрирован».
#### Экран создания/редактирования поста
Только авторизованный пользователь может попасть на этот экран.

Он включает в себя:

* Поле для ввода текста;

* Кнопку выбора локации, которая открывает фрагмент с картой;

* Кнопку для выбора упомянутых пользователей (открывает экран со списком и возможностью множественного выбора);

* Кнопки для выбора изображения: фото или галерея;

* Кнопки для выбора вложения: аудио или видео;

* Кнопку «Сохранить» в AppBar.

Обратите внимание, что размер вложений не должен превышать 15 МБ.

После создания поста пользователь возвращается к списку опубликованных сообщений.
#### Экран создания/редактирования события
Доступ к этому экрану возможен только для авторизованных пользователей.

На экране вы найдете:

* Поле для ввода текста;

* Кнопку выбора локации, которая открывает фрагмент с картой;

* Радиокнопку для выбора типа мероприятия — онлайн или офлайн (по умолчанию выбрано онлайн);

* Кнопку для выбора даты проведения события;

* Кнопки для выбора изображения: фото или галерея;

* Кнопки для добавления аудио- и видеофайлов;

* Кнопку выбора спикеров, которая открывает диалог со списком пользователей;

* Кнопку «Сохранить» в AppBar.

#### Экран просмотра стены
Стена представляет собой список постов, написанных одним автором. Её внешний вид
аналогичен карточкам на стартовом экране.

#### Экран просмотра пользователя
Экран содержит:

имя и логин в AppBar;
фото пользователя;
табы с выбором между стеной и работами пользователя.

#### Экран просмотра своего профиля
На этом экране вы найдете:

Имя и логин пользователя в AppBar; Фото пользователя;

* Табы для переключения между стеной и работами пользователя.


#### Инструменты / дополнительные материалы

Android Studio
Kotlin
Retrofit
Dagger Hilt
Swagger
Room
Glide
Figma
MapKit SDK
Git + GitHub.