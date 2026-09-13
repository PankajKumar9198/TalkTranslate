package com.talktranslate.service;

import com.talktranslate.model.ChatMessage;
import com.talktranslate.model.Friendship;
import com.talktranslate.model.FriendshipStatus;
import com.talktranslate.model.MessageType;
import com.talktranslate.model.TranslationResponse;
import com.talktranslate.model.User;
import com.talktranslate.repository.ChatMessageRepository;
import com.talktranslate.repository.FriendshipRepository;
import com.talktranslate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

        @Mock
        private ChatMessageRepository chatMessageRepository;

        @Mock
        private UserRepository userRepository;

        @Mock
        private FriendshipRepository friendshipRepository;

        @Mock
        private TranslationService translationService;

        @InjectMocks
        private ChatService chatService;

        private User userRahul;
        private User userSarah;
        private User userPriya;
        private Friendship mutualFriendship;

        @BeforeEach
        void setUp() {
                userRahul = User.builder()
                                .id("usr_rahul")
                                .username("rahul_sharma")
                                .fullName("Rahul Sharma")
                                .preferredLanguage("hi")
                                .build();

                userSarah = User.builder()
                                .id("usr_sarah")
                                .username("sarah_jenkins")
                                .fullName("Sarah Jenkins")
                                .preferredLanguage("en")
                                .build();

                userPriya = User.builder()
                                .id("usr_priya")
                                .username("priya_singh")
                                .fullName("Priya Singh")
                                .preferredLanguage("hinglish")
                                .build();

                mutualFriendship = Friendship.builder()
                                .id("f_123")
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.ACCEPTED)
                                .build();
        }

        @Test
        void shouldThrowExceptionWhenChatMessageIsNull() {
                assertThatThrownBy(() -> chatService.processAndSaveMessage(null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Message text cannot be empty");

                verifyNoInteractions(userRepository, friendshipRepository, translationService, chatMessageRepository);
        }

        @ParameterizedTest(name = "Reject invalid original text: [{0}]")
        @NullAndEmptySource
        @ValueSource(strings = { " ", "   ", "\t", "\n" })
        void shouldRejectNullOrBlankOriginalText(String invalidText) {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText(invalidText)
                                .build();

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Message text cannot be empty");

                verifyNoInteractions(userRepository, friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenSenderIdIsNull() {
                ChatMessage message = ChatMessage.builder()
                                .senderId(null)
                                .recipientId("usr_sarah")
                                .originalText("Hello Sarah")
                                .build();

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Sender ID and Recipient ID are required");

                verifyNoInteractions(userRepository, friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenRecipientIdIsNull() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId(null)
                                .originalText("Hello Sarah")
                                .build();

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Sender ID and Recipient ID are required");

                verifyNoInteractions(userRepository, friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenBothSenderAndRecipientIdAreNull() {
                ChatMessage message = ChatMessage.builder()
                                .senderId(null)
                                .recipientId(null)
                                .originalText("Hello")
                                .build();

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("Sender ID and Recipient ID are required");

                verifyNoInteractions(userRepository, friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenSenderNotFound() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_non_existent")
                                .recipientId("usr_sarah")
                                .originalText("Hello")
                                .build();

                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Sender not found with ID: usr_non_existent");

                verify(userRepository, times(1)).findById("usr_non_existent");
                verify(userRepository, never()).findById("usr_sarah");
                verifyNoInteractions(friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenRecipientNotFound() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_non_existent")
                                .originalText("Hello")
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_non_existent")).thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(NoSuchElementException.class)
                                .hasMessage("Recipient not found with ID: usr_non_existent");

                verify(userRepository, times(1)).findById("usr_rahul");
                verify(userRepository, times(1)).findById("usr_non_existent");
                verifyNoInteractions(friendshipRepository, translationService, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenSenderAndRecipientAreNotMutualFriends() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("Hello stranger")
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.processAndSaveMessage(message))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("Cannot send message: You and sarah_jenkins are not mutual friends");

                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
                verifyNoInteractions(translationService, chatMessageRepository);
        }

        @Test
        void shouldTranslateHindiToEnglishWithExplicitSenderLanguage() {
                ChatMessage incomingMessage = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("नमस्ते, आप कैसे हैं?")
                                .senderLang("hi")
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));

                TranslationResponse translationResponse = new TranslationResponse(
                                "नमस्ते, आप कैसे हैं?",
                                "Hello, how are you?",
                                "hi",
                                "en",
                                "hi",
                                false);

                when(translationService.translate("नमस्ते, आप कैसे हैं?", "hi", "en")).thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(incomingMessage);

                assertAll("Validate Processed Chat Message Fields",
                                () -> assertThat(processed).isNotNull(),
                                () -> assertThat(processed.getSenderName()).isEqualTo("Rahul Sharma"),
                                () -> assertThat(processed.getRecipientName()).isEqualTo("Sarah Jenkins"),
                                () -> assertThat(processed.getOriginalText()).isEqualTo("नमस्ते, आप कैसे हैं?"),
                                () -> assertThat(processed.getTranslatedText()).isEqualTo("Hello, how are you?"),
                                () -> assertThat(processed.getSenderLang()).isEqualTo("hi"),
                                () -> assertThat(processed.getRecipientLang()).isEqualTo("en"),
                                () -> assertThat(processed.getDetectedLang()).isEqualTo("hi"),
                                () -> assertThat(processed.getType()).isEqualTo(MessageType.CHAT));

                verify(translationService, never()).detectLanguage(any());
                verify(translationService, times(1)).translate("नमस्ते, आप कैसे हैं?", "hi", "en");
                verify(chatMessageRepository, times(1)).save(incomingMessage);
        }

        @Test
        void shouldTranslateEnglishReplyToHindiForRecipient() {
                ChatMessage replyMessage = ChatMessage.builder()
                                .senderId("usr_sarah")
                                .recipientId("usr_rahul")
                                .originalText("I am doing great, thank you!")
                                .senderLang("en")
                                .build();

                Friendship friendship = Friendship.builder()
                                .requester(userRahul)
                                .addressee(userSarah)
                                .status(FriendshipStatus.ACCEPTED)
                                .build();

                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(friendshipRepository.findAcceptedFriendship("usr_sarah", "usr_rahul"))
                                .thenReturn(Optional.of(friendship));

                TranslationResponse translationResponse = new TranslationResponse(
                                "I am doing great, thank you!",
                                "मैं बहुत अच्छा कर रहा हूँ, धन्यवाद!",
                                "en",
                                "hi",
                                "en",
                                false);

                when(translationService.translate("I am doing great, thank you!", "en", "hi"))
                                .thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(replyMessage);

                assertThat(processed.getSenderName()).isEqualTo("Sarah Jenkins");
                assertThat(processed.getRecipientName()).isEqualTo("Rahul Sharma");
                assertThat(processed.getTranslatedText()).isEqualTo("मैं बहुत अच्छा कर रहा हूँ, धन्यवाद!");
                assertThat(processed.getSenderLang()).isEqualTo("en");
                assertThat(processed.getRecipientLang()).isEqualTo("hi");
                assertThat(processed.getDetectedLang()).isEqualTo("en");
                assertThat(processed.getType()).isEqualTo(MessageType.CHAT);

                verify(translationService, never()).detectLanguage(any());
                verify(translationService, times(1)).translate("I am doing great, thank you!", "en", "hi");
                verify(chatMessageRepository, times(1)).save(replyMessage);
        }

        @Test
        void shouldTranslateMessageWhenRecipientPreferredLanguageIsHinglish() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_sarah")
                                .recipientId("usr_priya")
                                .originalText("Hello, how are you?")
                                .senderLang("en")
                                .build();

                Friendship friendship = Friendship.builder()
                                .requester(userSarah)
                                .addressee(userPriya)
                                .status(FriendshipStatus.ACCEPTED)
                                .build();

                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(userRepository.findById("usr_priya")).thenReturn(Optional.of(userPriya));
                when(friendshipRepository.findAcceptedFriendship("usr_sarah", "usr_priya"))
                                .thenReturn(Optional.of(friendship));

                TranslationResponse translationResponse = new TranslationResponse(
                                "Hello, how are you?",
                                "Namaste, aap kaise hain?",
                                "en",
                                "hinglish",
                                "en",
                                false);

                when(translationService.translate("Hello, how are you?", "en", "hinglish"))
                                .thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(message);

                assertThat(processed.getSenderName()).isEqualTo("Sarah Jenkins");
                assertThat(processed.getRecipientName()).isEqualTo("Priya Singh");
                assertThat(processed.getTranslatedText()).isEqualTo("Namaste, aap kaise hain?");
                assertThat(processed.getRecipientLang()).isEqualTo("hinglish");
                assertThat(processed.getType()).isEqualTo(MessageType.CHAT);
        }

        @Test
        void shouldAutoDetectSenderLanguageWhenSenderLangIsNull() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("शुभ प्रभात")
                                .senderLang(null)
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));
                when(translationService.detectLanguage("शुभ प्रभात")).thenReturn("hi");

                TranslationResponse translationResponse = new TranslationResponse(
                                "शुभ प्रभात",
                                "Good morning",
                                "hi",
                                "en",
                                "hi",
                                false);

                when(translationService.translate("शुभ प्रभात", "hi", "en")).thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(message);

                assertThat(processed.getSenderLang()).isEqualTo("hi");
                assertThat(processed.getTranslatedText()).isEqualTo("Good morning");

                verify(translationService, times(1)).detectLanguage("शुभ प्रभात");
                verify(translationService, times(1)).translate("शुभ प्रभात", "hi", "en");
        }

        @ParameterizedTest(name = "Auto-detect when senderLang is blank: [{0}]")
        @ValueSource(strings = { "", "   ", "\t" })
        void shouldAutoDetectSenderLanguageWhenSenderLangIsBlank(String blankSenderLang) {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("शुभ प्रभात")
                                .senderLang(blankSenderLang)
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));
                when(translationService.detectLanguage("शुभ प्रभात")).thenReturn("hi");

                TranslationResponse translationResponse = new TranslationResponse(
                                "शुभ प्रभात",
                                "Good morning",
                                "hi",
                                "en",
                                "hi",
                                false);

                when(translationService.translate("शुभ प्रभात", "hi", "en")).thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(message);

                assertThat(processed.getSenderLang()).isEqualTo("hi");
                assertThat(processed.getTranslatedText()).isEqualTo("Good morning");

                verify(translationService, times(1)).detectLanguage("शुभ प्रभात");
                verify(translationService, times(1)).translate("शुभ प्रभात", "hi", "en");
        }

        @ParameterizedTest(name = "Auto-detect when senderLang is auto keyword: [{0}]")
        @ValueSource(strings = { "auto", "AUTO", "Auto", "aUtO" })
        void shouldAutoDetectSenderLanguageWhenSenderLangIsAuto(String autoSenderLang) {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("शुभ प्रभात")
                                .senderLang(autoSenderLang)
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));
                when(translationService.detectLanguage("शुभ प्रभात")).thenReturn("hi");

                TranslationResponse translationResponse = new TranslationResponse(
                                "शुभ प्रभात",
                                "Good morning",
                                "hi",
                                "en",
                                "hi",
                                false);

                when(translationService.translate("शुभ प्रभात", "hi", "en")).thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage processed = chatService.processAndSaveMessage(message);

                assertThat(processed.getSenderLang()).isEqualTo("hi");
                assertThat(processed.getTranslatedText()).isEqualTo("Good morning");

                verify(translationService, times(1)).detectLanguage("शुभ प्रभात");
                verify(translationService, times(1)).translate("शुभ प्रभात", "hi", "en");
        }

        @Test
        void shouldPopulateAndPersistAllMessageMetadataAccurately() {
                ChatMessage message = ChatMessage.builder()
                                .senderId("usr_rahul")
                                .recipientId("usr_sarah")
                                .originalText("अलविदा और अपना ख्याल रखें")
                                .senderLang("hi")
                                .build();

                when(userRepository.findById("usr_rahul")).thenReturn(Optional.of(userRahul));
                when(userRepository.findById("usr_sarah")).thenReturn(Optional.of(userSarah));
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));

                TranslationResponse translationResponse = new TranslationResponse(
                                "अलविदा और अपना ख्याल रखें",
                                "Goodbye and take care",
                                "hi",
                                "en",
                                "hi",
                                false);

                when(translationService.translate("अलविदा और अपना ख्याल रखें", "hi", "en"))
                                .thenReturn(translationResponse);
                when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(i -> i.getArgument(0));

                ChatMessage result = chatService.processAndSaveMessage(message);

                ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
                verify(chatMessageRepository, times(1)).save(messageCaptor.capture());
                ChatMessage captured = messageCaptor.getValue();

                assertThat(captured).isSameAs(result);
                assertThat(captured.getSenderName()).isEqualTo("Rahul Sharma");
                assertThat(captured.getRecipientName()).isEqualTo("Sarah Jenkins");
                assertThat(captured.getSenderLang()).isEqualTo("hi");
                assertThat(captured.getRecipientLang()).isEqualTo("en");
                assertThat(captured.getDetectedLang()).isEqualTo("hi");
                assertThat(captured.getTranslatedText()).isEqualTo("Goodbye and take care");
                assertThat(captured.getType()).isEqualTo(MessageType.CHAT);
        }

        @Test
        void shouldThrowExceptionWhenUserAIsNullInChatHistory() {
                assertThatThrownBy(() -> chatService.getChatHistory(null, "usr_sarah"))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("User IDs cannot be null");

                verifyNoInteractions(friendshipRepository, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenUserBIsNullInChatHistory() {
                assertThatThrownBy(() -> chatService.getChatHistory("usr_rahul", null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("User IDs cannot be null");

                verifyNoInteractions(friendshipRepository, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenBothUserAAndUserBAreNullInChatHistory() {
                assertThatThrownBy(() -> chatService.getChatHistory(null, null))
                                .isInstanceOf(IllegalArgumentException.class)
                                .hasMessage("User IDs cannot be null");

                verifyNoInteractions(friendshipRepository, chatMessageRepository);
        }

        @Test
        void shouldThrowExceptionWhenUsersAreNotMutualFriendsInChatHistory() {
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.empty());

                assertThatThrownBy(() -> chatService.getChatHistory("usr_rahul", "usr_sarah"))
                                .isInstanceOf(IllegalStateException.class)
                                .hasMessage("Users are not mutual friends");

                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
                verifyNoInteractions(chatMessageRepository);
        }

        @Test
        void shouldReturnChatHistoryBetweenMutualFriends() {
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));

                ChatMessage m1 = ChatMessage.builder().id("m1").originalText("Hello").translatedText("नमस्ते").build();
                ChatMessage m2 = ChatMessage.builder().id("m2").originalText("नमस्ते").translatedText("Hello").build();

                when(chatMessageRepository.findChatHistoryBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(List.of(m1, m2));

                List<ChatMessage> history = chatService.getChatHistory("usr_rahul", "usr_sarah");

                assertThat(history).hasSize(2);
                assertThat(history.get(0).getId()).isEqualTo("m1");
                assertThat(history.get(0).getOriginalText()).isEqualTo("Hello");
                assertThat(history.get(1).getId()).isEqualTo("m2");
                assertThat(history.get(1).getOriginalText()).isEqualTo("नमस्ते");

                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
                verify(chatMessageRepository, times(1)).findChatHistoryBetween("usr_rahul", "usr_sarah");
        }

        @Test
        void shouldReturnEmptyListWhenNoChatHistoryExistsBetweenFriends() {
                when(friendshipRepository.findAcceptedFriendship("usr_rahul", "usr_sarah"))
                                .thenReturn(Optional.of(mutualFriendship));
                when(chatMessageRepository.findChatHistoryBetween("usr_rahul", "usr_sarah"))
                                .thenReturn(Collections.emptyList());

                List<ChatMessage> history = chatService.getChatHistory("usr_rahul", "usr_sarah");

                assertThat(history).isNotNull().isEmpty();

                verify(friendshipRepository, times(1)).findAcceptedFriendship("usr_rahul", "usr_sarah");
                verify(chatMessageRepository, times(1)).findChatHistoryBetween("usr_rahul", "usr_sarah");
        }
}
