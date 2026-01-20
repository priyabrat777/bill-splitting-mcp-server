package com.billsplitting.mcp.tools;

import com.billsplitting.entity.GroupMember;
import com.billsplitting.service.GroupMemberService;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMemberTools {

    private final GroupMemberService groupMemberService;

    public GroupMemberTools(GroupMemberService groupMemberService) {
        this.groupMemberService = groupMemberService;
    }

    @McpTool(name = "add_group_member", description = "Add a member to an expense group")
    public AddGroupMemberResponse addGroupMember(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName,
            @McpArg(name = "memberName", description = "Name of the member to add", required = true) String memberName) {
        GroupMember member = groupMemberService.addMember(groupName, memberName);
        return new AddGroupMemberResponse(member.getId(), member.getMemberName(), 
                member.getGroup().getName(), member.getCreatedAt().toString());
    }

    @McpTool(name = "remove_group_member", description = "Remove a member from an expense group")
    public RemoveGroupMemberResponse removeGroupMember(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName,
            @McpArg(name = "memberName", description = "Name of the member to remove", required = true) String memberName) {
        groupMemberService.removeMember(groupName, memberName);
        return new RemoveGroupMemberResponse(groupName, memberName, "Member removed successfully");
    }

    @McpTool(name = "list_group_members", description = "List all members in an expense group")
    public List<GroupMemberSummary> listGroupMembers(
            @McpArg(name = "groupName", description = "Name of the expense group", required = true) String groupName) {
        return groupMemberService.listMembers(groupName).stream()
                .map(member -> new GroupMemberSummary(member.getId(), member.getMemberName(), 
                        member.getCreatedAt().toString()))
                .collect(Collectors.toList());
    }

    public record AddGroupMemberResponse(
            Long id,
            String memberName,
            String groupName,
            String createdAt
    ) {}

    public record RemoveGroupMemberResponse(
            String groupName,
            String memberName,
            String message
    ) {}

    public record GroupMemberSummary(
            Long id,
            String memberName,
            String createdAt
    ) {}
}