<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator;

use Illuminate\Support\ServiceProvider;
use Jubianchi\NativePHP\LottieRefreshIndicator\Commands\CopyAnimationsCommand;

class LottieRefreshIndicatorServiceProvider extends ServiceProvider
{
    public function register(): void
    {
        $this->mergeConfigFrom(__DIR__.'/../config/lottie-refresh-indicator.php', 'lottie-refresh-indicator');
    }

    public function boot(): void
    {
        if ($this->app->runningInConsole()) {
            $this->commands([CopyAnimationsCommand::class]);

            $this->publishes([
                __DIR__.'/../config/lottie-refresh-indicator.php' => config_path('lottie-refresh-indicator.php'),
            ], 'lottie-refresh-indicator-config');
        }
    }
}
