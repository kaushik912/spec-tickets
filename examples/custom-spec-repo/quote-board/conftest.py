import copy

import pytest

import app as app_module


@pytest.fixture(autouse=True)
def reset_quotes():
    original_quotes = copy.deepcopy(app_module.quotes)
    original_next_id = app_module._next_id
    yield
    app_module.quotes[:] = original_quotes
    app_module._next_id = original_next_id
