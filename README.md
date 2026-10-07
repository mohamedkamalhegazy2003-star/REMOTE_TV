# Remote TV 📺

ريموت كنترول احترافي لأجهزة Android TV (بما فيها Android TV المثبت على لابتوب).
التصميم داكن مع أنيميشن (ضغط زراير، دخول متدرج، نبض الاتصال، توهج الباور) وهزّة خفيفة عند اللمس.

## طريقة بناء الـ APK على GitHub
1. اعمل Repository جديد وارفع محتوى المجلد ده كله (فك الضغط الأول).
2. روح تبويب **Actions** ← **Build APK** ← **Run workflow** (أو أي push يشغّله تلقائي).
3. بعد ما يخلص: افتح الـ run ونزّل **RemoteTV-apk** من Artifacts، جواه `app-debug.apk`.

(أو محلياً بـ Android Studio: افتح المجلد واضغط Run.)

## تجهيز الـ Android TV (مرة واحدة)
1. من إعدادات الجهاز: About ← اضغط 7 مرات على Build number لتفعيل Developer options.
2. Developer options ← فعّل **USB debugging** و **ADB over network / Wireless debugging** (منفذ 5555).
   - لو الخيار مش موجود (Android-x86 / BlissOS): افتح Terminal على الجهاز ونفّذ:
     `su -c "setprop service.adb.tcp.port 5555; stop adbd; start adbd"`
3. اللابتوب والموبايل على نفس شبكة الواي فاي.
4. في التطبيق: اضغط على حالة الاتصال ← **Scan network** أو اكتب الـ IP ← **Connect**.
5. أول مرة هتظهر رسالة على الشاشة "Allow USB debugging?" ← علّم **Always allow** ← OK.

## الوظائف
- أرقام، Vol، Mute، CH، مايك، D-pad + OK مع الضغط المستمر للتكرار، Home/Back/Input/Settings، الألوان الأربعة.
- أزرار التطبيقات: YouTube / Prime Video / Spotify / شاهد / Watch IT — التطبيق بيتحدد تلقائياً من المثبت على الجهاز، وزرار Apps بيفتح قائمة التطبيقات.
- زرار الباور: يطلب تأكيد ثم **Shutdown للابتوب** (أو Sleep).
- زرار `…` : Play/Pause، Forward، Guide، Info، Menu، وكتابة نص على الشاشة.
- أزرار الصوت في الموبايل بتتحكم في صوت التلفزيون.
- إعادة اتصال تلقائية لو الشبكة قطعت، وحفظ آخر IP.

## ملاحظات
- الاتصال بيتم عبر ADB (مكتبة dadb) لأنه الأضمن والأدق مع Android TV على اللابتوب، وهو اللي بيسمح بأمر Shutdown الحقيقي.
- لوجوهات التطبيقات الرسمية: حط الصور في `res/drawable` واستبدل الدوال في `Brands.kt` بـ `Image(painterResource(...))`.
- لتغيير اسم العنوان: `RemoteScreen.kt` ← `Text("Remote TV")`.
