package com.andpack.impl;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.SeekBar;

import com.andframe.$;
import com.andframe.api.DialogBuilder;
import com.andframe.api.DialogBuilder.OnDateSetVerifyListener;
import com.andframe.api.pager.Pager;
import com.andframe.api.viewer.ViewQuery;
import com.andframe.caches.AfPrivateCaches;
import com.andframe.feature.AfIntent;
import com.andframe.listener.SafeListener;
import com.andframe.task.AfDispatcher;
import com.andframe.util.java.AfDateFormat;
import com.andpack.activity.ApFragmentActivity;
import com.andpack.application.ApApp;
import com.lzy.imagepicker.ImagePicker;
import com.lzy.imagepicker.bean.ImageItem;
import com.lzy.imagepicker.ui.ImageGridActivity;
import com.lzy.imagepicker.view.CropImageView;

import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public class ApCommonBarBinder {

    //<editor-fold desc="接口定义">
    public interface ClickHook {
        /**
         * @return true 将会拦截点击事件 false 不会拦截
         */
        boolean onBinderClick(Binder binder);
    }
    public interface SelectBind {
        void text(Binder binder, String text, int which);
    }

    public interface MultiChoiceBind {
        void text(Binder binder, String text, int count, boolean[] checkedItems);
    }

    public interface TextBind {
        void text(Binder binder, String text);
    }

    public interface DateBind {
        void text(Binder binder, Date date);
    }

    public interface CheckBind {
        void check(Binder binder, boolean isChecked);
    }

    public interface SeekBind {
        void seek(Binder binder, int value, boolean fromUser);
    }
    public interface InputBind {
        void onBind(Binder binder, String value);
    }
    public interface ImageBind {
        /**
         * @param binder Binder 对象
         * @param path 图片路径
         * @return true 已经显示图片（Binder 将不会自动显示） false （Binder 将会自动显示）
         */
        boolean image(Binder binder, String path);
    }

    public interface TextVerify {
        String verify(String text) throws VerifyException;
    }

    public interface DateVerify {
        void verify(Date date) throws VerifyException;
    }

    public interface MultiChoiceVerify {
        void verify(int count, boolean[] checkedItems) throws VerifyException;
    }

    public static class VerifyException extends Exception {
        public VerifyException(String message) {
            super(message);
        }
    }
    //</editor-fold>

    private Pager pager;
    private String hintPrefix = "";
    private AfPrivateCaches caches;
    private ViewQuery<? extends ViewQuery> query;

    public ApCommonBarBinder(Pager pager) {
        this.pager = pager;
        this.query = $.query(pager);
        this.caches = AfPrivateCaches.getInstance(pager.getClass().getName());
    }

    public void setHintPrefix(String hintPrefix) {
        this.hintPrefix = hintPrefix;
    }

    public ViewQuery<? extends ViewQuery> $(Integer id, int... ids) {
        return query.$(id, ids);
    }
    public ViewQuery<? extends ViewQuery> $(View... views) {
        return query.$(views);
    }

    public TextBinder text(@IdRes int idvalue) {
        return new TextBinder(idvalue);
    }

    public InputBinder input(@IdRes int idvalue) {
        return new InputBinder(idvalue);
    }

    public SelectBinder select(@IdRes int idvalue, CharSequence[] items) {
        return new SelectBinder(idvalue, items);
    }

    public CheckBinder check(@IdRes int idvalue) {
        return new CheckBinder(idvalue);
    }

    public SwitchBinder Switch(@IdRes int idvalue) {
        return new SwitchBinder(idvalue);
    }

    public SeekBarBinder seek(@IdRes int idvalue) {
        return new SeekBarBinder(idvalue);
    }

    public DateBinder date(@IdRes int idvalue) {
        return new DateBinder(idvalue);
    }

    public MultiChoiceBinder multiChoice(@IdRes int idvalue, CharSequence[] items) {
        return new MultiChoiceBinder(idvalue, items);
    }

    public ActivityBinder activity(@IdRes int idvalue, Class<? extends Activity> clazz, Object... args) {
        return new ActivityBinder(idvalue, clazz, args);
    }

    public FragmentBinder fragment(@IdRes int idvalue, Class<? extends Fragment> clazz, Object... args) {
        return new FragmentBinder(idvalue, clazz, args);
    }

    public ImageBinder image(@IdRes int idimage) {
        return new ImageBinder(idimage);
    }


    public abstract class Binder<T extends Binder, LASTVAL> implements View.OnClickListener{
        public int idvalue;
        public String key = null;
        public CharSequence hintPrefix = ApCommonBarBinder.this.hintPrefix;
        public CharSequence hint = hintPrefix;
        public CharSequence name = "";
        public Binder next;
        public LASTVAL lastval;
        public Runnable start;
        public ClickHook clickHook;

        Binder(int idvalue) {
            this.idvalue = idvalue;
            $(idvalue).clicked(this);
        }

        public T click(int idclick) {
            $(idclick).clicked(this);
            $(idvalue).clicked(null).clickable(false);
            return self();
        }

        public T click(View view) {
            $(view).clicked(this);
            $(idvalue).clicked(null).clickable(false);
            return self();
        }

        public T clickHook(ClickHook clickHook) {
            this.clickHook = clickHook;
            return self();
        }

        //<editor-fold desc="提示信息">
        CharSequence getName(String dname, String[] names) {
            return names.length > 0 ? names[0] : (TextUtils.isEmpty(name) ? dname : name);
        }

        public T hintPrefix(CharSequence hintPrefix) {
            this.hintPrefix = hintPrefix;
            return hint(hintPrefix + name.toString());
        }

        public T name(CharSequence name) {
            this.name = name;
            return hint(hintPrefix + name.toString());
        }

        public T nameTextViewId(@IdRes int id) {
            this.name = $(id).text();
            return hint(hintPrefix + name.toString());
        }

        public T nameResId(@StringRes int id) {
            this.name = pager.getContext().getString(id);
            return hint(hintPrefix + name.toString());
        }

        public T hint(CharSequence hint) {
            this.hint = hint;
            return self();
        }

        public T hintTextViewId(@IdRes int id) {
            this.hint = hintPrefix + $(id).text();
            return self();
        }

        public T hintResId(@StringRes int id) {
            this.hint = hintPrefix + pager.getContext().getString(id);
            return self();
        }
        //</editor-fold>

        public T cache(Object... keys) {
            if (keys.length == 0) {
                key = String.valueOf(idvalue);
            } else {
                key = String.valueOf(keys[0]);
            }
            onRestoreCache(key);
            return self();
        }

        @SuppressWarnings("unused")
        public void onRestoreCache(String key) {

        }

        @SuppressWarnings("unused")
        public <Next extends Binder> Next next(Next next) {
            this.next = next;
            return next;
        }

        @Override
        public void onClick(View v) {
            performStart();
        }

        private void performStart() {
            if (clickHook != null && clickHook.onBinderClick(this)) {
                return;
            }
            if (start == null) {
                start();
            } else {
                start.run();
            }
            if (next != null) {
                next.performStart();
            }
        }

        public T start(Runnable start) {
            this.start = start;
            return self();
        }

        protected abstract void start();

        T self() {
            //noinspection unchecked
            return (T)this;
        }
    }

    public class SelectBinder extends Binder<SelectBinder, Void> implements DialogInterface.OnClickListener {

        private SelectBind bind;
        private final CharSequence[] items;

        SelectBinder(int idvalue, CharSequence[] items) {
            super(idvalue);
            this.items = items;
            this.hintPrefix("请选择");
        }

        @Override
        public void start() {
            $.dialog(pager).selectItem(hint, items, this);
        }

        @Override
        public void onRestoreCache(String key) {
            int i = caches.getInt(key, -1);
            value(i);
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            $(idvalue).text(items[which]);
            if (key != null && dialog != null) {
                caches.put(key, which);
            }
            if (bind != null) {
                bind.text(this, items[which].toString(), which);
            }
        }

        public SelectBinder bind(SelectBind bind) {
            this.bind = bind;
            return self();
        }

        public SelectBinder value(int index) {
            if (index >= 0 && index < items.length) {
                onClick(null, index);
            }
            return self();
        }

        public SelectBinder value(String value) {
            for (int i = 0; i < items.length; i++) {
                if (TextUtils.equals(items[i].toString(), value)) {
                    onClick(null, i);
                }
            }
            return self();
        }
    }

    public class MultiChoiceBinder extends Binder<MultiChoiceBinder, Void> implements DialogInterface.OnClickListener {

        private boolean[] checkedItems;
        private CharSequence[] items;
        private MultiChoiceBind bind;
        private MultiChoiceVerify verify;

        MultiChoiceBinder(int idvalue, CharSequence[] items) {
            super(idvalue);
            this.items = items;
            this.checkedItems = new boolean[items.length];
            this.hintPrefix("请选择");
        }

        public MultiChoiceBinder verify(MultiChoiceVerify verify) {
            this.verify = verify;
            return self();
        }

        @Override
        public void start() {
            $.dialog(pager).multiChoice(hint, items, checkedItems, null, this);
        }

        @Override
        public void onRestoreCache(String key) {
            List<Boolean> list = caches.getList(key, Boolean.class);
            if (list != null && list.size() == checkedItems.length) {
                for (int i = 0; i < checkedItems.length; i++) {
                    checkedItems[i] = list.get(i);
                }
                onClick(null, 0);
            }
        }

        public MultiChoiceBinder value(String text) {
            for (int i = 0; i < items.length && text != null; i++) {
                checkedItems[i] = text.contains(items[i]);
                onClick(null, 0);
            }
            return self();
        }

        public MultiChoiceBinder value(boolean... checkedItems) {
            if (checkedItems.length == this.checkedItems.length) {
                this.checkedItems = checkedItems;
                onClick(new Dialog(pager.getContext()), 0);
            }
            return self();
        }

        public MultiChoiceBinder bind(MultiChoiceBind bind) {
            this.bind = bind;
            return self();
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            int count = 0;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < items.length; i++) {
                if (checkedItems[i]) {
                    count++;
                    if (builder.length() > 0) {
                        builder.append(',');
                    }
                    builder.append(items[i].toString());
                }
            }
            if (verify != null) {
                try {
                    verify.verify(count, checkedItems);
                } catch (VerifyException e) {
                    pager.makeToastShort(e.getMessage());
                    return;
                }
            }
            $(idvalue).text(builder);
            if (key != null && dialog != null) {
                List<Boolean> list = new ArrayList<>(checkedItems.length);
                for (boolean bool : checkedItems) {
                    list.add(bool);
                }
                caches.putList(key, list);
            }
            if (bind != null) {
                bind.text(this, builder.toString(), count, checkedItems);
            }
        }
    }

    public class InputBinder extends Binder<InputBinder, String> implements TextWatcher {

        private InputBind bind;

        InputBinder(int idvalue) {
            super(idvalue);
        }

        public InputBinder inputType(int type) {
            $(idvalue).inputType(type);
            return self();
        }

        public InputBinder bind(InputBind bind) {
            this.bind = bind;
            $(idvalue).textChanged(this);
            return self();
        }
        @Override
        protected void start() {

        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }
        @Override
        public void afterTextChanged(Editable s) {
            caches.put(key, s.toString());
            AfDispatcher.dispatch(() -> {
                if (bind != null) {
                    bind.onBind(this, s.toString());
                }
            });
        }

        @Override
        public void onRestoreCache(String key) {
            value(caches.getString(key, ""));
        }

        public InputBinder value(String value) {
            $(idvalue).text(value);
            return self();
        }
    }

    public class TextBinder extends Binder<TextBinder, String> implements DialogBuilder.InputTextListener {

        private int type = InputType.TYPE_CLASS_TEXT;
        private TextBind bind;
        private TextVerify verify;
        private String valueSuffix = "";

        TextBinder(int idvalue) {
            super(idvalue);
            if (TextUtils.isEmpty(hintPrefix)) {
                this.hintPrefix("请输入");
            }
        }

        @Override
        public void start() {
            $.dialog(pager).inputText(hint, lastval == null ? $(idvalue).text().replace(valueSuffix,"") : lastval, type, this);
        }

        public TextBinder value(Object text) {
            if (text != null && !TextUtils.isEmpty(text.toString())) {
                onInputTextComfirm(null, text.toString());
            }
            return self();
        }

        @Override
        public void onRestoreCache(String key) {
            String text = caches.getString(key, null);
            if (text != null) {
                onInputTextComfirm(null, text);
            }
        }

        @Override
        public boolean onInputTextComfirm(EditText input, String value) {
            if (verify != null && input != null) {
                try {
                    value = verify.verify(value);
                } catch (VerifyException e) {
                    pager.makeToastShort(e.getMessage());
                    return false;
                }
            }
            lastval = value;
            $(idvalue).text(value + valueSuffix);
            if (key != null && input != null) {
                caches.put(key, value);
            }
            if (bind != null) {
                bind.text(this, value);
            }
            return true;
        }

        public TextBinder inputType(int type) {
            this.type = type;
            return self();
        }

        public TextBinder valueSuffix(String valueSuffix) {
            this.valueSuffix = valueSuffix;
            return self();
        }

        public TextBinder bind(TextBind bind) {
            this.bind = bind;
            return self();
        }

        //<editor-fold desc="输入验证">
        /**
         * 自定义验证规则
         */
        public TextBinder verify(TextVerify verify) {
            this.verify = verify;
            return self();
        }
        /**
         * 指定不为空
         */
        public TextBinder verifyNotEmpty(String... names) {
            CharSequence name = getName("值", names);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text.trim())) {
                    throw new VerifyException(name + "不能为空");
                }
                return text;
            });
        }

        /**
         * 指定为姓名的验证格式
         */
        public TextBinder verifyPersonName(String... names) {
            CharSequence name = getName("姓名", names);
            inputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException(name + "不能为空");
                }
                Pattern numex = Pattern.compile("\\d");
                if (numex.matcher(text).find()) {
                    throw new VerifyException(name + "中不能有数字");
                }
                boolean hasch = Pattern.compile("[\\u4e00-\\u9fa5]").matcher(text).find();
                boolean hasen = Pattern.compile("[a-zA-Z]").matcher(text).find();
                if (hasch && hasen) {
                    throw new VerifyException("中文" + name + "不能有混有英文");
                }
                if (text.getBytes(Charset.forName("gbk")).length > 16) {
                    throw new VerifyException(name + "不能超过8个汉字或16个字符");
                }
                return text;
            });
        }

        /**
         * 指定为手机号码验证格式
         */
        public TextBinder verifyPhone(String... names) {
            CharSequence name = getName("手机号码", names);
            inputType(InputType.TYPE_CLASS_PHONE);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                if (!text.matches("1[345789]\\d{9}")) {
                    throw new VerifyException("请输入正确的" + name);
                }
                return text;
            });
        }
        /**
         * 指定为Int
         */
        public TextBinder verifyInt(String... names) {
            CharSequence name = getName("数值", names);
            inputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                try {
                    text = String.valueOf(Integer.parseInt(text));
                } catch (NumberFormatException e) {
                    throw new VerifyException("请输入正确的" + name);
                }
                return text;
            });
        }
        /**
         * 指定为Uint
         */
        public TextBinder verifyUint(String... names) {
            CharSequence name = getName("数值", names);
            inputType(InputType.TYPE_CLASS_NUMBER);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                try {
                    float v = Integer.parseInt(text);
                    if (v < 0) {
                        throw new VerifyException(name + "不能是负数");
                    }
                    text = String.valueOf(v);
                } catch (NumberFormatException e) {
                    throw new VerifyException("请输入正确的" + name);
                }
                return text;
            });
        }
        /**
         * 指定为Float
         */
        public TextBinder verifyFloat(String... names) {
            CharSequence name = getName("数值", names);
            inputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL|InputType.TYPE_NUMBER_FLAG_SIGNED);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                try {
                    text = String.valueOf(Float.parseFloat(text));
                } catch (NumberFormatException e) {
                    throw new VerifyException("请输入正确的" + name);
                }
                return text;
            });
        }
        /**
         * 指定为Ufloat
         */
        public TextBinder verifyUfloat(String... names) {
            CharSequence name = getName("数值", names);
            inputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                try {
                    float v = Float.parseFloat(text);
                    if (v < 0) {
                        throw new VerifyException(name + "不能是负数");
                    }
                    text = String.valueOf(v);
                } catch (NumberFormatException e) {
                    throw new VerifyException("请输入正确的" + name);
                }
                return text;
            });
        }
        /**
         * 指定为身份证的验证格式
         */
        public TextBinder verifyIdNumber(String... names) {
            CharSequence name = getName("身份证号", names);
            inputType(InputType.TYPE_CLASS_TEXT);
            return this.verify(text -> {
                if (TextUtils.isEmpty(text)) {
                    throw new VerifyException("请输入" + name);
                }
                int[] n = new int[]{1, 0, (int)'x', 9, 8, 7, 6, 5, 4, 3, 2};
                int[] b = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                if (text.length() != 15 && text.length() != 18) {
                    throw new VerifyException(name + "必须为 15 位或18位");
                }
                String o = text.length() == 18 ? text.substring(0, 17) : text.substring(0, 6) + "19" + text.substring(6, 14);//id.substring(6, 16);
                if(!o.matches("^\\d+$")){//if (!/^\d+$/.test(o)) {
                    throw new VerifyException(name + "除最后一位外，必须为数字！");
                }
                int y = Integer.valueOf(o.substring(6, 10));
                int m = Integer.valueOf(o.substring(10, 12)) - 1;
                int d = Integer.valueOf(o.substring(12, 14));
                Calendar birth = Calendar.getInstance();
                birth.set(Calendar.YEAR, y);
                birth.set(Calendar.MONTH,m);
                birth.set(Calendar.DAY_OF_MONTH, d);
                int ly = birth.get(Calendar.YEAR);
                int lm = birth.get(Calendar.MONTH);
                int ld = birth.get(Calendar.DAY_OF_MONTH);
                Calendar now = Calendar.getInstance();
                if (ly != y || lm != m || ld != d || birth.after(now) || now.get(Calendar.YEAR) - ly > 140) {
                    throw new VerifyException(name + "出生年月输入错误！");
                }
                int g = 0,h = 0;
                for (; g < 17; g++) {
                    h = h + Integer.valueOf(o.charAt(g)+"") * b[g];
                }
                o += ""+n[h %= 11];
                if (text.length() == 18 && !text.toLowerCase(Locale.ENGLISH).equals(o)) {
                    throw new VerifyException(name + "最后一位校验码输入错误，正确校验码为：" + o.substring(17, 18) + "！");
                }
                return text;
            });
        }
        //</editor-fold>
    }

    public class DateBinder extends Binder<DateBinder, Date> implements OnDateSetVerifyListener {

        private DateBind bind;
        private DateVerify verify;
        private DateFormat format = AfDateFormat.DATE;

        DateBinder(int idvalue) {
            super(idvalue);
        }

        @Override
        public void start() {
            $.dialog(pager).selectDate(hint, lastval == null ? new Date() : lastval, this);
        }

        @SuppressWarnings("unused")
        public DateBinder initNow() {
            return value(new Date());
        }

        public DateBinder value(Date date) {
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            onDateSet(null, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH));
            return self();
        }

        public DateBinder format(DateFormat format) {
            this.format = format;
            return self();
        }

        public DateBinder format(String format) {
            return format(new SimpleDateFormat(format, Locale.CHINA));
        }

        @Override
        public boolean onPreDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            if (verify != null && view != null) {
                try {
                    verify.verify(AfDateFormat.parser(year,month,dayOfMonth));
                } catch (VerifyException e) {
                    pager.makeToastShort(e.getMessage());
                    return false;
                }
            }
            return true;
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            lastval = AfDateFormat.parser(year, month, day);
            $(idvalue).text(format.format(lastval));
            if (bind != null) {
                bind.text(this, lastval);
            }
        }

        public DateBinder bind(DateBind bind) {
            this.bind = bind;
            return self();
        }

        /**
         * 自定义验证规则
         */
        public DateBinder verify(DateVerify verify) {
            this.verify = verify;
            return self();
        }
        /**
         * 指定为之后的时间
         */
        public void verifyAfterNow(String... names) {
            CharSequence name = getName("日期", names);
            this.verify(date -> {
                if (date.getTime() < System.currentTimeMillis()) {
                    throw new VerifyException(name + "不能是现在之前");
                }
            });
        }
        /**
         * 指定为今天之后的时间
         */
        public void verifyAfterToday(String... names) {
            CharSequence name = getName("日期", names);
            this.verify(date -> {
                long today = AfDateFormat.roundDate(new Date(new Date().getTime() + 24L * 60 * 60 * 1000)).getTime() - 1;
                if (date.getTime() < today) {
                    throw new VerifyException(name + "必须是今天以后");
                }
            });
        }
        /**
         * 指定为今天之后的时间
         */
        public void verifyAfterWithToday(String... names) {
            CharSequence name = getName("日期", names);
            this.verify(date -> {
                long today = AfDateFormat.roundDate(new Date()).getTime() - 1;
                if (date.getTime() < today) {
                    throw new VerifyException(name + "不能早于今天");
                }
            });
        }
    }

    public class CheckBinder extends Binder<CheckBinder, Boolean> {

        private CheckBind bind;

        CheckBinder(int idvalue) {
            super(idvalue);
            lastval = $(idvalue).isChecked();
        }

        @Override
        public void onRestoreCache(String key) {
            Boolean bool = caches.get(key, null, Boolean.class);
            if (bool != null) {
                value(bool);
            }
        }

        public CheckBinder value(boolean isChecked) {
            lastval = isChecked;
            $(idvalue).checked(isChecked);
            if (bind != null) {
                bind.check(this, isChecked);
            }
            return self();
        }

        @Override
        public void onClick(View v) {
            if (v != null && v.getId() != idvalue) {
                lastval = $(idvalue).toggle().isChecked();
            }
            super.onClick(v);
        }

        @Override
        public void start() {
            lastval = $(idvalue).isChecked();
            if (key != null) {
                caches.put(key, lastval);
            }
            if (bind != null) {
                bind.check(this, lastval);
            }
        }

        public CheckBinder bind(CheckBind bind) {
            this.bind = bind;
            return self();
        }
    }

    public class SwitchBinder extends CheckBinder {
        SwitchBinder(int idvalue) {
            super(idvalue);
        }
    }

    public class SeekBarBinder extends Binder<SeekBarBinder, Integer> implements SeekBar.OnSeekBarChangeListener {

        private SeekBind bind;

        SeekBarBinder(int idvalue) {
            super(idvalue);
            SeekBar view = $(idvalue).view(SeekBar.class);
            if (view != null) {
                view.setOnSeekBarChangeListener(new SafeListener((SeekBar.OnSeekBarChangeListener) this));
            }
        }

        public SeekBarBinder max(int max) {
            $(idvalue).max(max);
            return self();
        }

        public SeekBarBinder value(int value) {
            $(idvalue).progress(value);
            return self();
        }

        public SeekBarBinder bind(SeekBind bind) {
            this.bind = bind;
            return self();
        }

        @Override
        protected void start() {
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (bind != null) {
                bind.seek(this, progress, fromUser);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    public class ActivityBinder extends Binder<ActivityBinder, Void> {

        private final Object[] args;
        private Class<? extends Activity> activity;

        ActivityBinder(int idvalue, Class<? extends Activity> activity, Object... args) {
            super(idvalue);
            this.args = args;
            this.activity = activity;
        }

        @Override
        public void start() {
            pager.startActivity(activity, args);
        }

    }

    public class FragmentBinder extends Binder<FragmentBinder, Void> {

        private final Object[] args;
        private Class<? extends Fragment> fragment;

        FragmentBinder(int idvalue, Class<? extends Fragment> fragment, Object... args) {
            super(idvalue);
            this.args = args;
            this.fragment = fragment;
        }

        @Override
        public void start() {
            ApFragmentActivity.start(fragment,args);
        }

    }

    public class ImageBinder extends Binder<ImageBinder, Void> {

        private int outPutX = 0;           //裁剪保存宽度
        private int outPutY = 0;           //裁剪保存高度
        private int request_image = 1000;
        private ImageBind bind;
        private CropImageView.Style style = CropImageView.Style.RECTANGLE;

        ImageBinder(int idimage) {
            super(idimage);
        }

        @Override
        protected void start() {
            ImagePicker picker = ImagePicker.getInstance();
            picker.setMultiMode(false);
            picker.setShowCamera(true);
            picker.setStyle(style);
            if (outPutX > 0 && outPutY > 0) {
                picker.setCrop(true);
                picker.setOutPutX(outPutX);
                picker.setOutPutY(outPutY);

                DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();

                int focusWidth = metrics.widthPixels * 3 / 4;
                int focusHeight = focusWidth * outPutY / outPutX;

                if (focusHeight > metrics.heightPixels * 3 / 4) {
                    focusHeight = metrics.heightPixels * 3 / 4;
                    focusWidth = focusHeight * outPutX / outPutY;
                }

                picker.setFocusWidth(focusWidth);
                picker.setFocusHeight(focusHeight);
            } else {
                picker.setCrop(false);
            }
            pager.startActivityForResult(ImageGridActivity.class,request_image);
        }

        public ImageBinder image(String url) {
            $.query(pager).$(idvalue).image(url);
            return self();
        }

        public void requestimage(int request_image) {
            this.request_image = request_image;
        }

        public ImageBinder circle() {
            style = CropImageView.Style.CIRCLE;
            return self();
        }

        public ImageBinder cut(int... xy) {
            if (xy.length == 0) {
                outPutX = 800;
                outPutY = 800;
            } else if (xy.length == 1) {
                outPutX = xy[0];
                outPutY = xy[0];
            } else {
                outPutX = xy[0];
                outPutY = xy[1];
            }
            return self();
        }

        public void onActivityResult(AfIntent intent, int requestcode, int resultcode) {
            if (requestcode == request_image /*&& resultcode == Activity.RESULT_OK*/) {
                new CropImageView(ApApp.get()).setOnBitmapSaveCompleteListener(null);
                //noinspection unchecked
                List<ImageItem> images = (ArrayList<ImageItem>) intent.getSerializableExtra(ImagePicker.EXTRA_RESULT_ITEMS);
                if (images != null && images.size() > 0) {
                    if (bind != null && !bind.image(this, images.get(0).path)) {
                        $.query(pager).$(idvalue).image(images.get(0).path);
                    }
                } else {
                    pager.makeToastShort("没有数据");
                }
            }
        }

        public ImageBinder bind(ImageBind bind) {
            this.bind = bind;
            return self();
        }

    }

}