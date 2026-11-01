<?php

return [
    /*
    | The directory holding the app's Lottie animations, relative to the
    | app's root. Every .lottie and .json file in it is copied under
    | `animations/` in the native projects when the app is built, so
    | `resources/animations/brand/logo.lottie` is the `asset` option
    | `animations/brand/logo.lottie`.
    */
    'path' => env('LOTTIE_REFRESH_INDICATOR_PATH', 'resources/animations'),
];
