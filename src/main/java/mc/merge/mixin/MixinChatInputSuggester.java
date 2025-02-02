package mc.merge.mixin;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import mc.merge.ModCore;
import mc.merge.handler.ChatHandler;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggester {

    @Shadow @Final TextFieldWidget textField; //input

    @Shadow @Final private List<OrderedText> messages; //commandUsage

    @Shadow private ParseResults<CommandSource> parse; //currentParse

    @Shadow private CompletableFuture<Suggestions> pendingSuggestions; //pendingSuggestions

    @Shadow private ChatInputSuggestor.SuggestionWindow window; //suggestions

    @Shadow boolean completingSuggestions; //keepSuggestions

    @Shadow public abstract void show(boolean narrateFirstSuggestion);

    @Inject(method = "refresh", at = @At("HEAD"), cancellable = true)
    private void refresh(CallbackInfo ci) {
        // Anything that is present in the input text before the cursor position
        String prefix = this.textField.getText().substring(0, Math.min(this.textField.getText().length(), this.textField.getCursor()));

        if (!prefix.startsWith(ModCore.COMMAND_PREFIX)) {
            return;
        }

        List<String> generatedSuggestions = ChatHandler.INSTANCE.getSuggestions(prefix);
        if (prefix.split(" ").length == 1 && !prefix.endsWith(" ")) {
            generatedSuggestions = generatedSuggestions.stream().map(s -> ModCore.COMMAND_PREFIX + s).toList();
        }
        if(!generatedSuggestions.isEmpty()) {
            ci.cancel();

            this.parse = null; // stop coloring

            if (this.completingSuggestions) { // aka, the user is tabbing through the suggestion list
                return;
            }

            this.textField.setSuggestion(null); // clear old suggestions
            this.window = null;
            // TODO: Support populating the usage text
            this.messages.clear();

            StringRange range = StringRange.between(prefix.lastIndexOf(" ") + 1, prefix.length()); // if there is no space this starts at 0

            List<Suggestion> suggestionList = Stream.of(generatedSuggestions.toArray(new String[0]))
                    .map(s -> new Suggestion(range, s))
                    .collect(Collectors.toList());

            Suggestions suggestions = new Suggestions(range, suggestionList);

            this.pendingSuggestions = new CompletableFuture<>();
            this.pendingSuggestions.complete(suggestions);
            this.show(true);
        }
    }
}