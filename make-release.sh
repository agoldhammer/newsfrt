lein clean
lein cljsbuild once min
cp ~/Prog/nooze/app/static/app.js ~/Prog/nooze/app/static/app.js.old
cp resources/public/js/compiled/app2.js ~/Prog/nooze/app/static/app.js

