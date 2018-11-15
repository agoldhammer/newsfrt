echo "Cleaning old compile artifacts"
lein clean
echo "Compiling with min profile"
lein cljsbuild once min
echo "Saving old app.js as app.js.old in nooze/app/static"
cp ~/Prog/nooze/app/static/app.js ~/Prog/nooze/app/static/app.js.old
echo "Copying newly compiled app2.js to nooze/app/static/app.js"
cp resources/public/js/compiled/app2.js ~/Prog/nooze/app/static/app.js

