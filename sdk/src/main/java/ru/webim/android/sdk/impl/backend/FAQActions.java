package ru.webim.android.sdk.impl.backend;

import androidx.annotation.NonNull;

import ru.webim.android.sdk.FAQ;
import ru.webim.android.sdk.impl.items.FAQCategoryItem;
import ru.webim.android.sdk.impl.items.FAQItemItem;
import ru.webim.android.sdk.impl.items.FAQSearchItemItem;
import ru.webim.android.sdk.impl.items.FAQStructureItem;
import ru.webim.android.sdk.impl.items.responses.DefaultResponse;

import java.util.List;

import retrofit2.Call;

public class FAQActions {
    @NonNull
    private final FAQRequestLoop requestLoop;
    @NonNull
    private final FAQService faq;
    private static final String CHARACTERS_TO_ENCODE = "\n!#$&'()*+,/:;=?@[] \"%-.<>\\^_`{|}~";

    FAQActions(@NonNull FAQService faq, @NonNull FAQRequestLoop faqRequestLoop) {
        this.faq = faq;
        this.requestLoop = faqRequestLoop;
    }

    private void enqueue(FAQRequestLoop.WebimRequest<?> request) {
        requestLoop.enqueue(request);
    }

    public void getItem(final String itemId,
                        final String deviceId,
                        @NonNull final DefaultCallback<FAQItemItem> callback) {
        callback.getClass(); // NPE
        enqueue(new FAQRequestLoop.WebimRequest<FAQItemItem>(true) {
            @Override
            public Call<FAQItemItem> makeRequest() {
                return faq.getItem(itemId, deviceId);
            }

            @Override
            public void runCallback(FAQItemItem response) {
                callback.onSuccess(response);
            }
        });
    }

    public void getStructure(final String structureId,
                            @NonNull final DefaultCallback<FAQStructureItem> callback) {
        callback.getClass(); // NPE
        enqueue(new FAQRequestLoop.WebimRequest<FAQStructureItem>(true) {
            @Override
            public Call<FAQStructureItem> makeRequest() {
                return faq.getStructure(structureId);
            }

            @Override
            public void runCallback(FAQStructureItem response) {
                callback.onSuccess(response);
            }
        });
    }

    public void getCategory(final String categoryId,
                            final String deviceId,
                            @NonNull final DefaultCallback<FAQCategoryItem> callback) {
        callback.getClass(); // NPE
        enqueue(new FAQRequestLoop.WebimRequest<FAQCategoryItem>(true) {
            @Override
            public Call<FAQCategoryItem> makeRequest() {
                return faq.getCategory(categoryId, deviceId);
            }

            @Override
            public void runCallback(FAQCategoryItem response) {
                callback.onSuccess(response);
            }
        });
    }

    public void getSearch(final String query, final String categoryId, final int limit,
                          @NonNull final DefaultCallback<List<FAQSearchItemItem>> callback) {
        callback.getClass(); //NPE
        enqueue(new FAQRequestLoop.WebimRequest<List<FAQSearchItemItem>>(true) {
            @Override
            public Call<List<FAQSearchItemItem>> makeRequest() {
                return faq.getSearch(percentEncode(query), categoryId, limit);
            }

            @Override
            public void runCallback(List<FAQSearchItemItem> response) {
                callback.onSuccess(response);
            }
        });
    }

    public void like(final String itemId, final String deviceId,
                     @NonNull final DefaultCallback<DefaultResponse> callback) {
        enqueue(new FAQRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest() {
                return faq.like(itemId, deviceId);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    public void dislike(final String itemId, final String deviceId,
                        @NonNull final DefaultCallback<DefaultResponse> callback) {
        enqueue(new FAQRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest() {
                return faq.dislike(itemId, deviceId);
            }

            @Override
            public void runCallback(DefaultResponse response) {
                callback.onSuccess(response);
            }
        });
    }

    public void track(final String itemId, FAQ.FAQItemSource openFrom) {
        final String open;
        switch (openFrom) {
            case SEARCH:
                open = "search";
                break;
            case TREE:
                open = "tree";
                break;
            default:
                open = "";
        }
        enqueue(new FAQRequestLoop.WebimRequest<DefaultResponse>(true) {
            @Override
            public Call<DefaultResponse> makeRequest() {
                return faq.track(itemId, open);
            }
        });
    }

    public void getCategoriesForApplication(final String application,
                                            final String language,
                                            final String departmentKey,
                                            final DefaultCallback<List<String>> callback) {
        enqueue(new FAQRequestLoop.WebimRequest<List<String>>(true) {
            @Override
            public Call<List<String>> makeRequest() {
                return faq.getCategoriesForApplication(application, "android", language, departmentKey);
            }

            @Override
            public void runCallback(List<String> response) {
                callback.onSuccess(response);
            }
        });

    }

    private static String percentEncode(String input) {
        if ((input == null) || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder(input);
        for (int i = (input.length() - 1); i >= 0; i--) {
            if (CHARACTERS_TO_ENCODE.indexOf(input.charAt(i)) != -1) {
                result.replace(
                        i,
                        (i + 1),
                        ("%" + Integer.toHexString(0x100 | input.charAt(i))
                                .substring(1).toUpperCase())
                );
            }
        }

        return result.toString();
    }
}
