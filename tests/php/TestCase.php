<?php

namespace Jubianchi\NativePHP\LottieRefreshIndicator\Tests;

use Jubianchi\NativePHP\LottieRefreshIndicator\LottieRefreshIndicatorServiceProvider;
use Orchestra\Testbench\TestCase as Orchestra;

abstract class TestCase extends Orchestra
{
    protected function getPackageProviders($app): array
    {
        return [LottieRefreshIndicatorServiceProvider::class];
    }
}
