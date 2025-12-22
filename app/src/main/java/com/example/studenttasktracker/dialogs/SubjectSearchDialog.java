package com.example.studenttasktracker.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.example.studenttasktracker.R;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubjectSearchDialog extends DialogFragment {

    private ListView listViewSubjects;
    private EditText etSearchSubject, etNewSubject;
    private Button btnAddSubject;
    private TextView tvSelectedSubject;
    private ArrayAdapter<String> adapter;
    private List<String> allSubjects;
    private Set<String> addedSubjects = new HashSet<>();
    private OnSubjectSelectedListener listener;

    public interface OnSubjectSelectedListener {
        void onSubjectSelected(String subject);
    }

    public static SubjectSearchDialog newInstance(String currentSubject, OnSubjectSelectedListener listener) {
        SubjectSearchDialog dialog = new SubjectSearchDialog();
        dialog.listener = listener;
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_subject_search_with_add, null);

        initViews(view);
        setupSubjectsList();
        setupSearch();
        setupAddButton();

        builder.setView(view)
                .setTitle("Выберите или добавьте предмет")
                .setNegativeButton("Отмена", (dialog, which) -> dismiss());

        return builder.create();
    }

    private void initViews(View view) {
        listViewSubjects = view.findViewById(R.id.listViewSubjects);
        etSearchSubject = view.findViewById(R.id.etSearchSubject);
        etNewSubject = view.findViewById(R.id.etNewSubject);
        btnAddSubject = view.findViewById(R.id.btnAddSubject);
    }

    private void setupSubjectsList() {
        // Полный список всех предметов
        allSubjects = new ArrayList<>(Arrays.asList(
                "Математика", "Алгебра", "Геометрия", "Физика", "Химия", "Биология",
                "Информатика", "Программирование", "Русский язык", "Литература",
                "Английский язык", "Немецкий язык", "Французский язык", "История",
                "Обществознание", "География", "Экономика", "Право", "Философия",
                "Психология", "Социология", "Искусство", "Музыка", "Рисование",
                "Черчение", "Физкультура", "ОБЖ", "Технология", "Астрономия",
                "Экология", "Краеведение", "Родной язык", "Мировая художественная культура",
                "Основы религиозных культур", "Бизнес-планирование", "Маркетинг",
                "Менеджмент", "Бухгалтерский учет", "Финансы", "Статистика",
                "Логика", "Риторика", "Латинский язык", "Архитектура", "Дизайн",
                "Журналистика", "Политология", "Международные отношения", "Культурология",
                "Антропология", "Археология", "Палеонтология", "Генетика",
                "Биохимия", "Нейробиология", "Квантовая физика", "Ядерная физика",
                "Органическая химия", "Неорганическая химия", "Аналитическая химия",
                "Высшая математика", "Математический анализ", "Линейная алгебра",
                "Дифференциальные уравнения", "Теория вероятностей", "Математическая статистика",
                "Дискретная математика", "Теория алгоритмов", "Базы данных",
                "Веб-разработка", "Мобильная разработка", "Искусственный интеллект",
                "Машинное обучение", "Кибербезопасность", "Сетевые технологии",
                "Операционные системы", "Компьютерные сети", "Компьютерная графика",
                "Разработка игр", "Тестирование ПО", "Управление проектами",
                "Системный анализ", "Электротехника", "Радиотехника", "Электроника",
                "Робототехника", "Нанотехнологии", "Биоинженерия", "Энергетика",
                "Строительство", "Архитектура", "Градостроительство", "Ландшафтный дизайн",
                "Геология", "Метеорология", "Океанология", "Ветеринария", "Агрономия",
                "Лесоводство", "Медицина", "Фармация", "Стоматология", "Педиатрия",
                "Хирургия", "Терапия", "Неврология", "Кардиология", "Онкология",
                "Психиатрия", "Гериатрия", "Спортивная медицина", "Юриспруденция",
                "Криминология", "Криминалистика", "Судебная экспертиза", "Нотариат",
                "Адвокатура", "Таможенное дело", "Налоговое право", "Трудовое право",
                "Семейное право", "Гражданское право", "Уголовное право",
                "Конституционное право", "Международное право", "Дипломатия",
                "Внешняя политика", "Военное дело", "Криптография", "Криптовалюты",
                "Блокчейн", "Цифровая экономика", "Электронная коммерция",
                "Интернет-маркетинг", "SMM", "SEO", "Копирайтинг", "Геймдизайн",
                "Саунд-дизайн", "Анимация", "Видеомонтаж", "Фотография", "Кинематограф",
                "Театр", "Хореография", "Вокал", "Игра на гитаре", "Игра на фортепиано",
                "Игра на скрипке", "Дирижирование", "Композиция", "Арт-терапия",
                "Логопедия", "Дефектология", "Социальная работа", "Педагогика",
                "Дошкольное образование", "Начальное образование", "Среднее образование",
                "Высшее образование", "Профессиональное образование", "Дополнительное образование",
                "Самообразование", "Иностранные языки", "Лингвистика", "Переводоведение",
                "Филология", "Литературоведение", "Языкознание", "Этнография",
                "Фольклористика", "Музееведение", "Архивоведение", "Библиотековедение",
                "Книговедение", "Журналистика", "Телевидение", "Радио", "PR", "Реклама",
                "Брендинг", "Мерчандайзинг", "Логистика", "Складское дело", "Транспорт",
                "Авиация", "Судовождение", "Железнодорожное дело", "Автомобилестроение",
                "Судостроение", "Авиастроение", "Космонавтика", "Ракетостроение",
                "Спутниковые системы", "Навигация", "Картография", "Геодезия",
                "Метрология", "Стандартизация", "Сертификация", "Качество", "Экспертиза",
                "Аудит", "Консалтинг", "Коучинг", "Тренинги", "Мотивация", "Тайм-менеджмент",
                "Самоменеджмент", "Лидерство", "Командообразование", "Переговоры",
                "Конфликтология", "Стратегическое планирование", "Тактическое планирование",
                "Оперативное управление", "Финансовый менеджмент", "Инвестиции",
                "Фондовый рынок", "Банковское дело", "Страхование", "Недвижимость",
                "Строительство", "Архитектура", "Дизайн интерьера", "Ландшафтный дизайн",
                "Флористика", "Садоводство", "Огородничество", "Пчеловодство",
                "Рыбоводство", "Животноводство", "Растениеводство", "Почвоведение",
                "Мелиорация", "Гидрология", "Метеорология", "Климатология", "Океанология",
                "Гляциология", "Вулканология", "Сейсмология", "Минералогия", "Петрография",
                "Геммология", "Палеонтология", "Антропология", "Археология", "Этнология",
                "Демография", "Статистика", "Социология", "Политология", "Культурология",
                "Регионоведение", "Востоковедение", "Африканистика", "Американистика",
                "Европеистика", "Славистика", "Германистика", "Романистика",
                "Классическая филология", "Византинистика", "Медиевистика",
                "История Древнего мира", "История Средних веков", "История Нового времени",
                "История Новейшего времени", "Отечественная история", "Всеобщая история",
                "История культуры", "История науки", "История техники", "История искусства",
                "История музыки", "История театра", "История кино", "История литературы",
                "История философии", "История религии", "История права", "Военная история",
                "Экономическая история", "Социальная история", "Политическая история",
                "Дипломатическая история", "Генеалогия", "Геральдика", "Нумизматика",
                "Фалеристика", "Вексиллология", "Сфрагистика", "Эпиграфика", "Палеография",
                "Кодикология", "Другое"
        ));

        Collections.sort(allSubjects);
        updateAdapter(allSubjects);

        listViewSubjects.setOnItemClickListener((parent, view, position, id) -> {
            String selectedSubject = adapter.getItem(position);
            selectSubject(selectedSubject);
        });
    }

    private void setupSearch() {
        etSearchSubject.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSubjects(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupAddButton() {
        btnAddSubject.setOnClickListener(v -> addNewSubject());

        // Добавляем возможность добавления по нажатию Enter
        etNewSubject.setOnEditorActionListener((v, actionId, event) -> {
            addNewSubject();
            return true;
        });
    }

    private void addNewSubject() {
        String newSubject = etNewSubject.getText().toString().trim();

        if (newSubject.isEmpty()) {
            etNewSubject.setError("Введите название предмета");
            return;
        }

        if (newSubject.length() < 2) {
            etNewSubject.setError("Название слишком короткое");
            return;
        }

        // Проверяем, нет ли уже такого предмета
        for (String subject : allSubjects) {
            if (subject.equalsIgnoreCase(newSubject)) {
                etNewSubject.setError("Такой предмет уже существует");
                return;
            }
        }

        // Добавляем новый предмет
        allSubjects.add(newSubject);
        Collections.sort(allSubjects);
        addedSubjects.add(newSubject);

        // Очищаем поле ввода
        etNewSubject.setText("");
        etNewSubject.clearFocus();

        // Показываем сообщение
        Toast.makeText(getContext(), "✅ Предмет добавлен: " + newSubject, Toast.LENGTH_SHORT).show();

        // Обновляем список
        updateAdapter(allSubjects);

        // Выбираем добавленный предмет
        selectSubject(newSubject);
    }

    private void filterSubjects(String query) {
        List<String> filteredList = new ArrayList<>();

        if (query.isEmpty()) {
            filteredList.addAll(allSubjects);
            Collections.sort(filteredList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (String subject : allSubjects) {
                if (subject.toLowerCase().contains(lowerCaseQuery)) {
                    filteredList.add(subject);
                }
            }
            Collections.sort(filteredList);
        }

        updateAdapter(filteredList);
    }

    private void updateAdapter(List<String> subjects) {
        adapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_list_item_1, subjects);
        listViewSubjects.setAdapter(adapter);
    }

    private void selectSubject(String subject) {
        if (listener != null) {
            listener.onSubjectSelected(subject);
        }

        // Показываем сообщение если предмет добавлен пользователем
        if (addedSubjects.contains(subject)) {
            Toast.makeText(getContext(), "✅ Использован ваш предмет: " + subject, Toast.LENGTH_SHORT).show();
        }

        dismiss();
    }
}